package com.ihsanguldur.coreledger.application.external.exception;

public class ExternalBankGatewayException extends RuntimeException {
    public ExternalBankGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
