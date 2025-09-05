package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.rest.exception.InSufficientFundsException;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dmurraysd.spring.wallet.logging.LoggingUtil.formatLogMessage;

@Component
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

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

    public Optional<Wallet> createAccount(IdProvider context) {
        UUID walletId = uuidSupplier.get();

        return this.createAccount(walletId.toString(), 0.0, context);
    }

    public Optional<Wallet> createAccount(String walletId, double walletBalance, IdProvider context) {
        logger.info(formatLogMessage(context, "Creating wallet with Id %s", walletId));

        Wallet newWallet = new Wallet(walletId, walletBalance);
        return cacheService.addToCache(newWallet, context);
    }

    public Optional<WalletTransaction> transferFunds(FundTransferRequest fundTransferRequest, IdProvider context) {
        logger.info(formatLogMessage(context, "Transferring funds with wallet Id %s", fundTransferRequest.walletId()));

        Optional<Wallet> customerWallet = cacheService.getIfPresent(fundTransferRequest.walletId(), context);

        if (customerWallet.isEmpty()) {
            customerWallet = this.createAccount(fundTransferRequest.walletId(), 0.0, context);
        }

        if(customerWallet.isPresent()) {
            Double newBalance = calculateNewBalance(fundTransferRequest, customerWallet);
            Wallet updatedWallet = new Wallet(customerWallet.get().walletId(), newBalance);
            if(Boolean.TRUE.equals(cacheService.put(updatedWallet, context))) {
                return walletTransactionService.saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context);
            }
        }

        return walletTransactionService.saveTransaction(fundTransferRequest, TransactionStatus.FAILURE, context);
    }

    public Optional<Double> retrieveBalance(String walletId, IdProvider context) {
        logger.info(formatLogMessage(context, "Wallet balance retrieval with wallet Id %s", walletId));
        return cacheService.getIfPresent(walletId, context)
                .map(Wallet::walletBalance);
    }

    public List<WalletTransaction> getAllTransactions(String walletId, IdProvider context) {
        logger.info(formatLogMessage(context, "Retrieving all trnsactions with wallet Id %s", walletId));
        return walletTransactionService.getAllTransactionsByWalletId(walletId, context);
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
