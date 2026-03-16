package com.fintouch.ledger.infra.exception;

public class UnprocessableBusinessException extends ApiException {

    public UnprocessableBusinessException(String code, String message) {
        super(code, message);
    }
}

