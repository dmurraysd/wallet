package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.cache.CacheConfig;
import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.logging.LoggingUtil;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.repository.WalletEntityMapper;
import com.dmurraysd.spring.wallet.repository.WalletRepository;
import com.dmurraysd.spring.wallet.util.config.RedisTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@EnableAutoConfiguration
@ContextConfiguration(classes = {RedisTestConfig.class, WalletCacheService.class, CacheConfig.class, WalletRepository.class, CacheLockingService.class})
@DataRedisTest(properties = {"spring.data.redis.port=6381",
        "spring.data.redis.host=localhost"})
class WalletCacheServiceTest {
    private static final String SOURCE_ID = "wallet-rest-api";
    public static final String CONTEXT_UUID = "a1d1429a-c68d-43e8-ac6d-9d62a1f47c03";
    private static final IdProvider context = LoggingUtil.loggingContext(UUID.fromString(CONTEXT_UUID), SOURCE_ID);

    @MockitoBean
    WalletRepository walletRepository;

    @Autowired
    private WalletCacheService walletCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Test
    void addToCacheAndSaveToRepository() {
        Wallet wallet = new Wallet("123", 100.0);
        when(walletRepository.save(any())).thenReturn(WalletEntityMapper.toEntity(wallet));

        Wallet actualWallet = walletCacheService.addToCache(wallet, context).get();

        verify(walletRepository).save(any());
        assertEquals(wallet, actualWallet);
    }

    @Test
    void shouldGetWalletIfPresentInCache() {
        Wallet wallet = new Wallet("123", 100.0);
        redisTemplate.opsForValue().set(wallet.walletId(), wallet);

        Wallet actualWallet = walletCacheService.getIfPresent(wallet.walletId(), context).get();

        verifyNoInteractions(walletRepository);
        assertEquals(wallet, actualWallet);
    }

    @Test
    void shouldGetWalletFromRespositoryIfNotPresentInCacheAndSaveToCache() {
        Wallet wallet = new Wallet("123", 100.0);
        when(walletRepository.findByWalletId(any())).thenReturn(Optional.of(WalletEntityMapper.toEntity(wallet)));

        Wallet actualWallet = walletCacheService.getIfPresent(wallet.walletId(), context).get();

        Wallet savedToCacheWallet = (Wallet) redisTemplate.opsForValue().get(wallet.walletId());
        verify(walletRepository).findByWalletId(any());
        assertEquals(wallet, actualWallet);
        assertEquals(wallet, savedToCacheWallet);
    }

    @Test
    void shouldPutInCacheAndSaveToRepository() {
        Wallet wallet = new Wallet("123", 100.0);
        when(walletRepository.save(any())).thenReturn(WalletEntityMapper.toEntity(wallet));

        Boolean isPutSuccess = walletCacheService.put(wallet, context);

        Wallet savedToCacheWallet = (Wallet) redisTemplate.opsForValue().get(wallet.walletId());
        verify(walletRepository).save(any());
        assertTrue(isPutSuccess);
        assertEquals(wallet, savedToCacheWallet);
    }

}