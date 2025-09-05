package com.dmurraysd.spring.wallet.util.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.function.Supplier;

@ActiveProfiles("test")
@TestConfiguration
public class WalletTestConfig {

    public static final String TEST_UUID = "59b35af9-99bc-45a2-9f2d-bfa00b48e098";

    public Supplier<UUID> getRandomUUID() {
        return () -> UUID.fromString(TEST_UUID);
    }
}
