package com.fintouch.ledger.service;

import com.fintouch.ledger.domain.User;
import com.fintouch.ledger.domain.UserType;
import com.fintouch.ledger.domain.Wallet;
import com.fintouch.ledger.infra.exception.NotFoundException;
import com.fintouch.ledger.infra.money.Money;
import com.fintouch.ledger.repository.UserRepository;
import com.fintouch.ledger.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public User createUser(String fullName, String email, String document, UserType type) {
        User user = userRepository.save(new User(fullName, email, document, type));
        walletRepository.save(new Wallet(user, Money.normalize(BigDecimal.ZERO)));
        return user;
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado"));
    }
}

