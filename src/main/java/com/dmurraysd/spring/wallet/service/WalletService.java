package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletService {
    public Wallet createAccount() {
        return null;
    }

    public WalletTransaction transferFunds(FundTransferRequest fundTransferRequest) {
        return null;
    }

    public double retrieveBalance(String accountId) {
        return -1;
    }

    public List<WalletTransaction> getAllTransactions(String amountId) {
        return List.of();
    }
}
