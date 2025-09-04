package com.dmurraysd.spring.wallet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class WalletApplicationConfig {

    @Bean
    public Supplier<UUID> uuidSupplier() {
        return UUID::randomUUID;
    }

    @Bean
    public Supplier<Long> longSupplier() {
        return System::currentTimeMillis;
    }
}
