package com.dmurraysd.spring.wallet.logging;

public interface IdProvider {
    String getCorrelationId();

    String getSourceId();
}
