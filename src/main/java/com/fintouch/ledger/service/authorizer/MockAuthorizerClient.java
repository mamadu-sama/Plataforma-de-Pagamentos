package com.fintouch.ledger.service.authorizer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "fintouch.authorizer", name = "mode", havingValue = "mock", matchIfMissing = true)
public class MockAuthorizerClient implements AuthorizerClient {

    @Override
    public boolean authorize(Long payerId, Long payeeId, BigDecimal value) {
        return true;
    }
}

