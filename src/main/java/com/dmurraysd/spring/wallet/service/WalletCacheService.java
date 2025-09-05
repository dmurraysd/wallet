package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.repository.WalletEntity;
import com.dmurraysd.spring.wallet.repository.WalletEntityMapper;
import com.dmurraysd.spring.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.dmurraysd.spring.wallet.logging.LoggingUtil.formatLogMessage;

@Component
public class WalletCacheService {

    private static final Logger logger = LoggerFactory.getLogger(WalletCacheService.class);

    private final Long timeOutInMills;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WalletRepository walletRepository;

    public WalletCacheService(@Value("${cache.expiry.in.mills:30000}") Long timeOutInMills,
                              RedisTemplate<String, Object> redisTemplate,
                              WalletRepository walletRepository) {
        this.timeOutInMills = timeOutInMills;
        this.redisTemplate = redisTemplate;
        this.walletRepository = walletRepository;
    }

    public Optional<Wallet> addToCache(Wallet walletRequest, IdProvider context) {
        try {
            logger.info(formatLogMessage(context, "Adding wallet to cache with wallet Id [%s]", walletRequest.walletId()));
            Optional<Wallet> persistedWallet = Optional.of(WalletEntityMapper.toEntity(walletRequest))
                    .map(walletRepository::save)
                    .map(WalletEntityMapper::toDTO);

            persistedWallet.ifPresent(wallet -> redisTemplate.opsForValue().set(wallet.walletId(), wallet, timeOutInMills, TimeUnit.MILLISECONDS));

            return persistedWallet;
        } catch (Exception e) {
            logger.error(formatLogMessage(context, "Error processing wallet creation to cache with wallet Id [%s] - [%s]", walletRequest.walletId(), e.getMessage()));
        }

        return Optional.empty();
    }

    public Optional<Wallet> getIfPresent(String walletId, IdProvider context) {
        try {
            logger.info(formatLogMessage(context, "Retrieving wallet from cache with wallet Id [%s]", walletId));

            Optional<Wallet> wallet = Optional.ofNullable(redisTemplate.opsForValue().get(walletId))
                    .map(Wallet.class::cast);

            if(wallet.isEmpty()) {
                wallet = walletRepository.findByWalletId(walletId)
                        .map(WalletEntityMapper::toDTO);

                wallet.ifPresent(w -> redisTemplate.opsForValue().set(walletId, w));
            }

            return wallet;
        } catch (Exception e) {
            logger.error(formatLogMessage(context, "Error processing wallet retrieval from cache with wallet Id [%s]- [%s]", walletId, e.getMessage()));
        }

        return Optional.empty();
    }

    public Boolean put(Wallet wallet, IdProvider context) {
        try {
            logger.info(formatLogMessage(context, "Updating wallet to cache with wallet Id [%s]", wallet.walletId()));

            redisTemplate.opsForValue().set(wallet.walletId(), wallet, timeOutInMills, TimeUnit.MILLISECONDS);

            Optional<WalletEntity> persistedWalletEntity = this.walletRepository.findByWalletId(wallet.walletId())
                            .map(walletEntity -> WalletEntityMapper.toUpdatedBalanceEntity(wallet, walletEntity))
                                    .map(walletRepository::save);
            if(persistedWalletEntity.isEmpty()) {
                walletRepository.save(WalletEntityMapper.toEntity(wallet));
            }

            return true;
        } catch (Exception e) {
            logger.error(formatLogMessage(context, "Retrieving wallet from cache with wallet Id [%s]- [%s]", wallet.walletId(), e.getMessage()));
        }

        return false;
    }
}
