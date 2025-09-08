package com.dmurraysd.spring.wallet.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends CrudRepository<WalletEntity, Long> {
    Optional<WalletEntity> findByWalletId(String walletId);
}
