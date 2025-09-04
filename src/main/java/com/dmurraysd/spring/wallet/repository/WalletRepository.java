package com.dmurraysd.spring.wallet.repository;

import com.dmurraysd.spring.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends CrudRepository<WalletEntity, Long> {
    Optional<WalletEntity> findByWalletId(String walletId);
}
