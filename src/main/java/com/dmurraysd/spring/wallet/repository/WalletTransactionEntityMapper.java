package com.dmurraysd.spring.wallet.repository;

import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;

public class WalletTransactionEntityMapper {

    public WalletTransactionEntityMapper() {
    }

    public static WalletTransaction toDTO(WalletTransactionEntity walletTransactionEntity) {
        return new WalletTransaction(walletTransactionEntity.getTransactionId(),
                walletTransactionEntity.getWalletId(),
                walletTransactionEntity.getAmount(),
                walletTransactionEntity.getTransactionType(),
                walletTransactionEntity.getTransactionStatus(),
                walletTransactionEntity.getZonedDateTime());
    }

    public static WalletTransactionEntity toEntity(WalletTransaction walletTransaction) {
        return new WalletTransactionEntity(walletTransaction.transactionId(),
                walletTransaction.walletId(),
                walletTransaction.amount(),
                walletTransaction.transactionType(),
                walletTransaction.transactionStatus(),
                walletTransaction.zonedDateTime());
    }
}
