package com.fintouch.ledger.infra.api;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        List<String> details,
        Instant timestamp
) {
}

