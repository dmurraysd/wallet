package com.dmurraysd.spring.wallet.model.transaction;

import java.time.Instant;

public record WalletTransaction(String transactionId, String walletId, Double amount, TransactionType transactionType,
                                TransactionStatus transactionStatus, Instant timestamp) {
}
