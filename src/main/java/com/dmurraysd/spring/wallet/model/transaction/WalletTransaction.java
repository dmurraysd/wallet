package com.dmurraysd.spring.wallet.model.transaction;

import java.time.ZonedDateTime;

public record WalletTransaction(String transactionId, String walletId, double amount, TransactionType transactionType, TransactionStatus transactionStatus, ZonedDateTime zonedDateTime) implements Transaction {
}
