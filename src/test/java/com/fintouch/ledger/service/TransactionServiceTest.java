package com.fintouch.ledger.service;

import com.fintouch.ledger.domain.Transaction;
import com.fintouch.ledger.domain.User;
import com.fintouch.ledger.domain.UserType;
import com.fintouch.ledger.domain.Wallet;
import com.fintouch.ledger.infra.exception.UnprocessableBusinessException;
import com.fintouch.ledger.repository.TransactionRepository;
import com.fintouch.ledger.repository.UserRepository;
import com.fintouch.ledger.repository.WalletRepository;
import com.fintouch.ledger.service.authorizer.AuthorizerClient;
import com.fintouch.ledger.service.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    @Test
    void merchant_nao_pode_ser_pagador() {
        UserRepository userRepository = mock(UserRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        AuthorizerClient authorizerClient = mock(AuthorizerClient.class);
        NotificationService notificationService = mock(NotificationService.class);

        TransactionService service = new TransactionService(userRepository, walletRepository, transactionRepository, authorizerClient, notificationService);

        User payer = new User("Lojista", "m@example.com", "DOC1", UserType.MERCHANT);
        User payee = new User("Recebedor", "r@example.com", "DOC2", UserType.COMMON);

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));

        UnprocessableBusinessException ex = assertThrows(UnprocessableBusinessException.class,
                () -> service.transfer(new BigDecimal("10.00"), 1L, 2L));
        assertEquals("MERCHANT_CANNOT_PAY", ex.getCode());
    }

    @Test
    void saldo_insuficiente_bloqueia_antes_de_autorizar() {
        UserRepository userRepository = mock(UserRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        AuthorizerClient authorizerClient = mock(AuthorizerClient.class);
        NotificationService notificationService = mock(NotificationService.class);

        TransactionService service = new TransactionService(userRepository, walletRepository, transactionRepository, authorizerClient, notificationService);

        User payer = new User("Pagador", "p@example.com", "DOC1", UserType.COMMON);
        User payee = new User("Recebedor", "r@example.com", "DOC2", UserType.MERCHANT);
        Wallet payerWallet = new Wallet(payer, new BigDecimal("9.00"));
        Wallet payeeWallet = new Wallet(payee, new BigDecimal("0.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(payerWallet));
        when(walletRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(payeeWallet));

        UnprocessableBusinessException ex = assertThrows(UnprocessableBusinessException.class,
                () -> service.transfer(new BigDecimal("10.00"), 1L, 2L));
        assertEquals("INSUFFICIENT_FUNDS", ex.getCode());
    }

    @Test
    void falha_na_notificacao_nao_invalida_transaccao() {
        UserRepository userRepository = mock(UserRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        AuthorizerClient authorizerClient = mock(AuthorizerClient.class);
        NotificationService notificationService = mock(NotificationService.class);

        TransactionService service = new TransactionService(userRepository, walletRepository, transactionRepository, authorizerClient, notificationService);

        User payer = new User("Pagador", "p@example.com", "DOC1", UserType.COMMON);
        User payee = new User("Recebedor", "r@example.com", "DOC2", UserType.MERCHANT);
        Wallet payerWallet = new Wallet(payer, new BigDecimal("200.00"));
        Wallet payeeWallet = new Wallet(payee, new BigDecimal("0.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));
        when(walletRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(payerWallet));
        when(walletRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(payeeWallet));
        when(authorizerClient.authorize(eq(1L), eq(2L), eq(new BigDecimal("150.00")))).thenReturn(true);

        Transaction saved = new Transaction(new BigDecimal("150.00"), payer, payee, Instant.now());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        doThrow(new RuntimeException("boom")).when(notificationService).notify(any(Transaction.class));

        assertDoesNotThrow(() -> service.transfer(new BigDecimal("150.00"), 1L, 2L));
    }
}
