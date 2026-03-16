package com.fintouch.ledger.service.authorizer;

import java.math.BigDecimal;

public interface AuthorizerClient {

    boolean authorize(Long payerId, Long payeeId, BigDecimal value);
}

