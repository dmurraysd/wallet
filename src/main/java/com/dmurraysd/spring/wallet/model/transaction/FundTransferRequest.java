package com.dmurraysd.spring.wallet.model.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FundTransferRequest(@NotBlank(message = "Invalid walletId : Wallet ID is empty") String walletId,
                                  @NotNull Double amount, @NotNull TransactionType transactionType) {
}
