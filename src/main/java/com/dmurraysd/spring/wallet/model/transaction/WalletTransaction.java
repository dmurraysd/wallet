package com.dmurraysd.spring.wallet.model.transaction;

import com.dmurraysd.spring.wallet.model.Wallet;

import java.time.ZonedDateTime;

public record WalletTransaction(String transactionId, Wallet account, double amount, TransactionType transactionType, ZonedDateTime zonedDateTime) implements Transaction {
}
