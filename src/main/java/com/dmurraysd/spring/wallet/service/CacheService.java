package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.model.Wallet;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CacheService {
    public Optional<Wallet> addToCache(Wallet expectedWallet) {
        return Optional.empty();
    }

    public Optional<Wallet> getIfPresent(String walletId) {
        return Optional.empty();
    }

    public Boolean put(Wallet wallet) {
        return false;
    }
}
