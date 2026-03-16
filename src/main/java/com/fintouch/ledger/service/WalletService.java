package com.fintouch.ledger.service;

import com.fintouch.ledger.domain.Wallet;
import com.fintouch.ledger.infra.exception.NotFoundException;
import com.fintouch.ledger.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet getByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Carteira não encontrada"));
    }
}

