package com.dmurraysd.spring.wallet.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Profile("test")
@TestConfiguration
public class WalletTestConfig {

    public static final String TEST_WALLET_UUID_2 = "7534614a-9e1b-44a5-aa9f-66f55ffdbb50";
    public static final String TEST_WALLET_UUID_3 = "8366e958-ec8f-4616-88b7-173ad86211f0";
    public static final String TEST_WALLET_UUID_4 = "6ffd4e3d-fa69-4704-96c1-51e302367575";

    public static final String SUPPLIED_TEST_UUID = "6ffd4e3d-fa69-4704-96c1-51e302367575";

    public static final Long TEST_LONG = 999862253000L;
    public static final String INSTANT_TIMESTAMP = "2023-05-03T12:42:14.971Z";

    @Bean
    public Supplier<UUID> getRandomUUID() {
        return () -> UUID.fromString(SUPPLIED_TEST_UUID);
    }

    @Bean
    public Supplier<Long> longSupplier() {
        return () -> TEST_LONG;
    }

    @Bean
    public Supplier<Instant> instantSupplier() {
        return () -> Instant.parse(INSTANT_TIMESTAMP);
    }

}
