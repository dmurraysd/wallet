package com.dmurraysd.spring.wallet.model.transaction;

import jakarta.validation.constraints.NotBlank;

public record FundTransferRequest(@NotBlank(message = "Invalid walletId : Wallet ID is empty") String walletId, double amount, TransactionType transactionType) {
}
