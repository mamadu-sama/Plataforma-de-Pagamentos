package com.fintouch.ledger.api.dto;

import java.math.BigDecimal;

public record WalletResponse(
        Long walletId,
        Long userId,
        BigDecimal balance
) {
}

