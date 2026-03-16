package com.fintouch.ledger.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        Long transactionId,
        BigDecimal value,
        Long payer,
        Long payee,
        Instant createdAt
) {
}

