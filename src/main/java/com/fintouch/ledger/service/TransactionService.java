package com.fintouch.ledger.service;

import com.fintouch.ledger.domain.Transaction;
import com.fintouch.ledger.domain.User;
import com.fintouch.ledger.domain.UserType;
import com.fintouch.ledger.domain.Wallet;
import com.fintouch.ledger.infra.exception.NotFoundException;
import com.fintouch.ledger.infra.exception.UnprocessableBusinessException;
import com.fintouch.ledger.infra.money.Money;
import com.fintouch.ledger.repository.TransactionRepository;
import com.fintouch.ledger.repository.UserRepository;
import com.fintouch.ledger.repository.WalletRepository;
import com.fintouch.ledger.service.authorizer.AuthorizerClient;
import com.fintouch.ledger.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuthorizerClient authorizerClient;
    private final NotificationService notificationService;

    public TransactionService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            AuthorizerClient authorizerClient,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.authorizerClient = authorizerClient;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction transfer(BigDecimal value, Long payerId, Long payeeId) {
        if (payerId.equals(payeeId)) {
            throw new UnprocessableBusinessException("INVALID_PARTIES", "Pagador e recebedor têm de ser diferentes");
        }

        BigDecimal normalized = Money.normalize(value);
        if (normalized == null || normalized.signum() <= 0) {
            throw new UnprocessableBusinessException("INVALID_VALUE", "O valor da transferência tem de ser positivo");
        }

        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new NotFoundException("Pagador não encontrado"));
        User payee = userRepository.findById(payeeId)
                .orElseThrow(() -> new NotFoundException("Recebedor não encontrado"));

        if (payer.getType() == UserType.MERCHANT) {
            throw new UnprocessableBusinessException("MERCHANT_CANNOT_PAY", "Lojistas não podem ser pagadores");
        }

        Long firstLock = payerId < payeeId ? payerId : payeeId;
        Long secondLock = payerId < payeeId ? payeeId : payerId;

        Wallet wallet1 = walletRepository.findByUserIdWithLock(firstLock)
                .orElseThrow(() -> new NotFoundException("Carteira não encontrada"));
        Wallet wallet2 = walletRepository.findByUserIdWithLock(secondLock)
                .orElseThrow(() -> new NotFoundException("Carteira não encontrada"));

        Wallet payerWallet = payerId.equals(firstLock) ? wallet1 : wallet2;
        Wallet payeeWallet = payeeId.equals(firstLock) ? wallet1 : wallet2;

        BigDecimal payerBalance = Money.normalize(payerWallet.getBalance());
        if (payerBalance.compareTo(normalized) < 0) {
            throw new UnprocessableBusinessException("INSUFFICIENT_FUNDS", "Saldo insuficiente");
        }

        boolean authorized = authorizerClient.authorize(payerId, payeeId, normalized);
        if (!authorized) {
            throw new UnprocessableBusinessException("NOT_AUTHORIZED", "Transacção não autorizada");
        }

        payerWallet.setBalance(Money.normalize(payerWallet.getBalance().subtract(normalized)));
        payeeWallet.setBalance(Money.normalize(payeeWallet.getBalance().add(normalized)));
        walletRepository.save(payerWallet);
        walletRepository.save(payeeWallet);

        Transaction tx = transactionRepository.save(new Transaction(normalized, payer, payee, Instant.now()));

        try {
            notificationService.notify(tx);
        } catch (Exception e) {
            log.warn("Falha ao enviar notificação para transacção {}: {}", tx.getId(), e.getMessage());
        }

        return tx;
    }
}

