package com.dmurraysd.spring.wallet.rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public WalletError<String> handleException(Throwable ex) {
        final String details = Objects.nonNull(ex) ? ex.getClass().getSimpleName() : "Unknown";

        final WalletError<String> walletError =
                new WalletError<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), details);
        logger.error(String.valueOf(walletError));
        return walletError;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public WalletError<Map<String, List<String>>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = Optional.of(ex.getBindingResult())
                .map(Errors::getFieldErrors)
                .stream()
                .flatMap(Collection::stream)
                .map(FieldError::getDefaultMessage)
                .toList();

        final Map<String, List<String>> details = Map.of("errors", errors);

        logger.error(String.valueOf(details));
        return new WalletError<>(HttpStatus.BAD_REQUEST.value(), "Validation error", details);
    }
}
