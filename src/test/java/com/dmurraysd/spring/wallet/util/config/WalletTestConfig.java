package com.dmurraysd.spring.wallet.util.config;

import org.springframework.boot.test.context.TestConfiguration;

import java.util.UUID;

@TestConfiguration
public class WalletTestConfig {

    public UUID getRandomUUID() {
        return UUID.randomUUID();
    }
}
