package com.dmurraysd.spring.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class RestExceptionHandler {

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

        return new WalletError<>(HttpStatus.BAD_REQUEST.value(), "Validation error", details);
    }
}
