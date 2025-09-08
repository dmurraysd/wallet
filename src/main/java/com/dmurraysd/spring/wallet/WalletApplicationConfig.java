package com.dmurraysd.spring.wallet;

import com.dmurraysd.spring.wallet.rest.WalletController;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class WalletApplicationConfig {

    @Profile("!test")
    @Bean
    public Supplier<UUID> uuidSupplier() {
        return UUID::randomUUID;
    }

    @Profile("!test")
    @Bean
    public Supplier<Instant> instantSupplier() {
        return Instant::now;
    }

    @Profile("!test")
    @Bean
    public Supplier<Long> longSupplier() {
        return System::currentTimeMillis;
    }

    @Bean
    public GroupedOpenApi liveEventTrackerApi() {
        return GroupedOpenApi.builder()
                .group("Wallet API")
                .pathsToMatch("/**")
                .packagesToScan(WalletController.class.getPackageName()).build();
    }

    @Primary
    @Bean
    public static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());
    }
}
