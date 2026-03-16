package com.fintouch.ledger.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "value é obrigatório")
        @DecimalMin(value = "0.01", message = "value tem de ser positivo")
        BigDecimal value,
        @NotNull(message = "payer é obrigatório")
        Long payer,
        @NotNull(message = "payee é obrigatório")
        Long payee
) {
}

