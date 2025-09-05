package com.dmurraysd.spring.wallet.exception;

public class InSufficientFundsException extends RuntimeException {
    public InSufficientFundsException(String message) {
        super(message);
    }
}
