package com.fintouch.ledger.infra.exception;

public class ExternalDependencyException extends ApiException {

    public ExternalDependencyException(String message) {
        super("EXTERNAL_DEPENDENCY", message);
    }
}

