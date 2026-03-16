package com.fintouch.ledger.infra.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final int SCALE = 2;

    private Money() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}

