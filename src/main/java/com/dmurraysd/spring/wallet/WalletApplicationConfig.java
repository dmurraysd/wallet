package com.dmurraysd.spring.wallet;

import com.dmurraysd.spring.wallet.rest.WalletController;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class WalletApplicationConfig {

    @Profile("!test")
    @Bean
    public Supplier<UUID> uuidSupplier() {
        return UUID::randomUUID;
    }

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
}
