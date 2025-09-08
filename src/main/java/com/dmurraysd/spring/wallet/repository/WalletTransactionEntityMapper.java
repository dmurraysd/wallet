package com.dmurraysd.spring.wallet.repository;

import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Supplier;

public class WalletTransactionEntityMapper {

    private WalletTransactionEntityMapper() {
    }

    public static WalletTransaction toDTO(WalletTransactionEntity walletTransactionEntity, Supplier<Instant> instantSupplier) {
        Instant timestamp = Optional.ofNullable(walletTransactionEntity.getTransactionTimestamp())
                .map(t -> Instant.ofEpochMilli(t).truncatedTo(ChronoUnit.MILLIS))
                .orElseGet(instantSupplier);

        return new WalletTransaction(walletTransactionEntity.getTransactionId(),
                walletTransactionEntity.getWalletId(),
                walletTransactionEntity.getAmount(),
                walletTransactionEntity.getTransactionType(),
                walletTransactionEntity.getTransactionStatus(),
                timestamp);
    }

    public static WalletTransactionEntity toEntity(WalletTransaction walletTransaction, Supplier<Long> longSupplier) {
        Long timestamp = Optional.ofNullable(walletTransaction.timestamp())
                .map(t -> t.truncatedTo(ChronoUnit.MILLIS).toEpochMilli())
                .orElseGet(longSupplier);

        return new WalletTransactionEntity(walletTransaction.transactionId(),
                walletTransaction.walletId(),
                walletTransaction.amount(),
                walletTransaction.transactionType(),
                walletTransaction.transactionStatus(),
                timestamp);
    }
}
