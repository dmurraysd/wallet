package com.dmurraysd.spring.wallet.rest.exception;

public record WalletError<T>(int code, String message, T details) {

    public WalletError<T> of(int code, String message, T details) {
        return new WalletError<>(code, message, details);
    }
}
