package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.exception.InSufficientFundsException;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.repository.WalletRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class WalletService {

    private final WalletCacheService cacheService;
    private final WalletTransactionService walletTransactionService;
    private final WalletRepository walletRepository;
    private final Supplier<UUID> uuidSupplier;
    private final Supplier<Long> longSupplier;

    public WalletService(WalletCacheService cacheService,
                         WalletTransactionService walletTransactionService,
                         WalletRepository walletRepository,
                         Supplier<UUID> uuidSupplier,
                         Supplier<Long> longSupplier) {
        this.cacheService = cacheService;
        this.walletTransactionService = walletTransactionService;
        this.walletRepository = walletRepository;
        this.uuidSupplier = uuidSupplier;
        this.longSupplier = longSupplier;
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
        ZonedDateTime transactionTimestamp = Instant.ofEpochMilli(longSupplier.get()).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));

        Optional<Wallet> customerWallet = cacheService.getIfPresent(fundTransferRequest.walletId());

        if (customerWallet.isEmpty()) {
            customerWallet = this.createAccount(fundTransferRequest.walletId(), 0.0);
        }

        if(customerWallet.isPresent()) {
            Double newBalance = calculateNewBalance(fundTransferRequest, customerWallet);
            Wallet updatedWallet = new Wallet(customerWallet.get().walletId(), newBalance);
            if(Boolean.TRUE.equals(cacheService.put(updatedWallet))) {
                return walletTransactionService.saveTransaction(Optional.of(updatedWallet), fundTransferRequest, fundTransferRequest.transactionType(), TransactionStatus.SUCCESS);
            }
        }

        return walletTransactionService.saveTransaction(Optional.empty(), fundTransferRequest, fundTransferRequest.transactionType(), TransactionStatus.FAILURE);
    }

    public Optional<Double> retrieveBalance(String walletId) {
        return cacheService.getIfPresent(walletId)
                .map(Wallet::walletBalance);
    }

    public List<WalletTransaction> getAllTransactions(String walletId) {
        return walletTransactionService.getAllTransactionsById(walletId);
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
