package com.dmurraysd.spring.wallet.logging;

import java.util.UUID;

import static java.lang.String.format;

public class LoggingUtil {

    private LoggingUtil() {
    }

    public static IdProvider loggingContext(UUID uuid, String sourceId) {
        return new IdContextProvider(uuid.toString(), sourceId);
    }

    public static String formatLogMessage(IdProvider loggingContext, String message, Object... args) {
        return String.format("correlationId [%s] sourceId [%s] - %s",
                loggingContext.getCorrelationId(), loggingContext.getSourceId(), format(message, args));
    }
}
