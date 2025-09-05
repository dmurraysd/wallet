package com.dmurraysd.spring.wallet.logging;

import java.util.Optional;

public record IdContextProvider(String correlationId, String sourceId) implements IdProvider {

    @Override
    public String getCorrelationId() {
        return Optional.ofNullable(this.correlationId).orElse("-");
    }

    @Override
    public String getSourceId() {
        return Optional.ofNullable(this.sourceId).orElse("-");
    }
}
