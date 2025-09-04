package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WalletTransactionService {

    public WalletTransactionService() {
    }

    public Optional<WalletTransaction> saveTransaction(Optional<Wallet> updatedWallet, FundTransferRequest fundTransferRequest, TransactionType transactionType, TransactionStatus transactionStatus) {
        return Optional.empty();
    }

    public List<WalletTransaction> getAllTransactionsById(String walletId) {
        return List.of();
    }
}
