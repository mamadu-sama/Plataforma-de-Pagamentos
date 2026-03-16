package com.fintouch.ledger.api;

import com.fintouch.ledger.api.dto.WalletResponse;
import com.fintouch.ledger.domain.Wallet;
import com.fintouch.ledger.service.WalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{id}")
    public WalletResponse get(@PathVariable("id") Long userId) {
        Wallet wallet = walletService.getByUserId(userId);
        return new WalletResponse(wallet.getId(), wallet.getUser().getId(), wallet.getBalance());
    }
}

