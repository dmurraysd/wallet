package com.dmurraysd.spring.wallet.service;


import com.dmurraysd.spring.wallet.WalletApplicationConfig;
import com.dmurraysd.spring.wallet.cache.CacheConfig;
import com.dmurraysd.spring.wallet.config.RedisTestConfig;
import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.logging.LoggingUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("locking-test")
@EnableAutoConfiguration
@ContextConfiguration(classes = {RedisTestConfig.class, CacheConfig.class, WalletApplicationConfig.class, CacheLockingService.class})
@DataRedisTest(properties = {"spring.data.redis.port=6380",
        "spring.data.redis.host=localhost",
        "lock.expiry.in.mills=1000"})
class CacheLockingServiceTest {

    private static final String SOURCE_ID = "wallet-rest-api";
    public static final String CONTEXT_UUID = "a1d1429a-c68d-43e8-ac6d-9d62a1f47c03";
    private static final IdProvider context = LoggingUtil.loggingContext(UUID.fromString(CONTEXT_UUID), SOURCE_ID);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheLockingService cacheLockingService;

    @Test
    void shouldAcquiredLock() {
        String walletId = UUID.randomUUID().toString();
        String lockValue = UUID.randomUUID().toString();

        Boolean isLockAcquired = cacheLockingService.acquireLock(walletId, lockValue, context);

        assertTrue(isLockAcquired);
    }

    @Test
    void shouldAcquiredLockAndNoOtherLocksAreHeld() {
        String walletId = UUID.randomUUID().toString();
        String lockValue = UUID.randomUUID().toString();

        Boolean isLockAcquired = cacheLockingService.acquireLock(walletId, lockValue, context);
        assertTrue(isLockAcquired);

        String lockValue2 = UUID.randomUUID().toString();
        isLockAcquired = cacheLockingService.acquireLock(walletId, lockValue2, context);
        assertFalse(isLockAcquired);
    }

    @Test
    void shouldReleaseLock() {
        String walletId = UUID.randomUUID().toString();
        String lockValue = UUID.randomUUID().toString();
        String lockPrefix = "lock";
        String lockKey = String.format("%s:%s", lockPrefix, walletId);

        cacheLockingService.acquireLock(walletId, lockValue, context);
        cacheLockingService.releaseLock(walletId, lockValue, context);

        assertNull(redisTemplate.opsForValue().get(lockKey));
    }

    @Test
    void shouldReleaseLockAfterExpired() {
        String walletId = UUID.randomUUID().toString();
        String lockValue = UUID.randomUUID().toString();
        String lockPrefix = "lock";
        String lockKey = String.format("%s:%s", lockPrefix, walletId);

        cacheLockingService.acquireLock(walletId, lockValue, context);

        await().pollDelay(Duration.ofSeconds(2L))
                .untilAsserted(() -> assertNull(redisTemplate.opsForValue().get(lockKey)));
    }

    @Test
    void shouldHaveLockBeforeExpired() {
        String walletId = UUID.randomUUID().toString();
        String lockValue = UUID.randomUUID().toString();
        String lockPrefix = "lock";
        String lockKey = String.format("%s:%s", lockPrefix, walletId);

        cacheLockingService.acquireLock(walletId, lockValue, context);

        await().pollDelay(Duration.ofMillis(200L)).atMost(Duration.ofSeconds(1L))
                .untilAsserted(() -> assertNotNull(redisTemplate.opsForValue().get(lockKey)));
    }
}
