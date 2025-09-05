package com.dmurraysd.spring.wallet.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends CrudRepository<WalletTransactionEntity, Long> {
    List<WalletTransactionEntity> findByWalletId(String walletId);
}
