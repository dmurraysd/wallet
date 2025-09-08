package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.dmurraysd.spring.wallet.logging.LoggingUtil.formatLogMessage;
import static java.util.Objects.nonNull;

@Component
public class CacheLockingService {

    private static final Logger logger = LoggerFactory.getLogger(CacheLockingService.class);

    private final Long expiryInMills;
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheLockingService(@Value("${lock.expiry.in.mills:30000}") Long expiryInMills,
                               RedisTemplate<String, Object> redisTemplate) {
        this.expiryInMills = expiryInMills;
        this.redisTemplate = redisTemplate;
    }


    public Boolean acquireLock(String walletId, String lockValue, IdProvider context) {
        logger.info(formatLogMessage(context, "Acquiring lock with wallet Id %s", walletId));
        String lockPrefix = "lock";
        String lockKey = String.format("%s:%s", lockPrefix, walletId);
        return Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expiryInMills, TimeUnit.MILLISECONDS))
                .orElse(Boolean.FALSE);
    }

    public void releaseLock(String walletId, String lockValue, IdProvider context) {
        logger.info(formatLogMessage(context, "Releasing lock with wallet Id %s", walletId));
        String lockPrefix = "lock";
        String lockKey = String.format("%s:%s", lockPrefix, walletId);

        String savedLockValue = (String) redisTemplate.opsForValue().get(lockKey);

        if (nonNull(savedLockValue) && savedLockValue.equals(lockValue)) {
            redisTemplate.delete(lockKey);
        }
    }
}
