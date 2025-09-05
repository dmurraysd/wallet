package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.exception.InSufficientFundsException;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class WalletService {

    private final WalletCacheService cacheService;
    private final WalletTransactionService walletTransactionService;
    private final Supplier<UUID> uuidSupplier;

    public WalletService(WalletCacheService cacheService,
                         WalletTransactionService walletTransactionService,
                         Supplier<UUID> uuidSupplier) {
        this.cacheService = cacheService;
        this.walletTransactionService = walletTransactionService;
        this.uuidSupplier = uuidSupplier;
    }

    public Optional<Wallet> createAccount() {
        UUID walletId = uuidSupplier.get();

        return this.createAccount(walletId.toString(), 0.0);
    }

    public Optional<Wallet> createAccount(String walletId, double walletBalance) {
        Wallet newWallet = new Wallet(walletId, walletBalance);
        return cacheService.addToCache(newWallet);
    }

    public Optional<WalletTransaction> transferFunds(FundTransferRequest fundTransferRequest) {
        Optional<Wallet> customerWallet = cacheService.getIfPresent(fundTransferRequest.walletId());

        if (customerWallet.isEmpty()) {
            customerWallet = this.createAccount(fundTransferRequest.walletId(), 0.0);
        }

        if(customerWallet.isPresent()) {
            Double newBalance = calculateNewBalance(fundTransferRequest, customerWallet);
            Wallet updatedWallet = new Wallet(customerWallet.get().walletId(), newBalance);
            if(Boolean.TRUE.equals(cacheService.put(updatedWallet))) {
                return walletTransactionService.saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS);
            }
        }

        return walletTransactionService.saveTransaction(fundTransferRequest, TransactionStatus.FAILURE);
    }

    public Optional<Double> retrieveBalance(String walletId) {
        return cacheService.getIfPresent(walletId)
                .map(Wallet::walletBalance);
    }

    public List<WalletTransaction> getAllTransactions(String walletId) {
        return walletTransactionService.getAllTransactionsByWalletId(walletId);
    }

    private Double calculateNewBalance(FundTransferRequest fundTransferRequest, Optional<Wallet> customerWallet) {
        Wallet wallet = customerWallet.get();
        double balance;
        if (TransactionType.DEPOSIT.equals(fundTransferRequest.transactionType())) {
            balance = wallet.walletBalance() + fundTransferRequest.amount();
        } else {
            if (wallet.walletBalance() < fundTransferRequest.amount()) {
                throw new InSufficientFundsException("Insufficient funds for wallet with Id: " + wallet.walletId());
            }
            balance = wallet.walletBalance() - fundTransferRequest.amount();
        }
        return balance;
    }
}
