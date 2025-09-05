package com.dmurraysd.spring.wallet.rest.exception;

public class InSufficientFundsException extends RuntimeException {
    public InSufficientFundsException(String message) {
        super(message);
    }
}
