package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.logging.LoggingUtil;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private static final String SOURCE_ID = "wallet-rest-api";
    public static final String CONTEXT_UUID = "a1d1429a-c68d-43e8-ac6d-9d62a1f47c03";
    private static final IdProvider context = LoggingUtil.loggingContext(UUID.fromString(CONTEXT_UUID), SOURCE_ID);

    public static final String TRANSACTION_UUID = "00000000-0000-0000-0000-000000000000";
    WalletCacheService cacheService = mock(WalletCacheService.class);
    WalletTransactionService walletTransactionService = mock(WalletTransactionService.class);
    WalletService walletService = new WalletService(cacheService, walletTransactionService, () -> UUID.fromString(TRANSACTION_UUID));

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldCreateAccountInRepositoryAndCache() {
        Wallet expectedWallet = new Wallet(TRANSACTION_UUID, 0.0);
        when(cacheService.addToCache(expectedWallet, context)).thenReturn(Optional.of(expectedWallet));

        Wallet actualWallet = walletService.createAccount(context).get();

        verify(cacheService).addToCache(expectedWallet, context);
        assertEquals(expectedWallet, actualWallet);
    }

    @Test
    void shouldCreateAccountWhenTransactionOccurs() {
        Wallet expectedWallet = new Wallet(TRANSACTION_UUID, 0.0);
        when(cacheService.addToCache(expectedWallet, context)).thenReturn(Optional.of(expectedWallet));

        Wallet actualWallet = walletService.createAccount(expectedWallet.walletId(), expectedWallet.walletBalance(), context).orElse(null);

        verify(cacheService).addToCache(expectedWallet, context);
        assertEquals(expectedWallet, actualWallet);
    }

    @Test
    void shouldTransferFundsByDeposit() {
        Wallet wallet = new Wallet(TRANSACTION_UUID, 0.0);
        Wallet updatedWallet = new Wallet(wallet.walletId(), wallet.walletBalance() + 50.0);
        double depositAmount = 50.0;
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(100001L).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), depositAmount, TransactionType.DEPOSIT);
        WalletTransaction expectedTransaction = new WalletTransaction(TRANSACTION_UUID, updatedWallet.walletId(), depositAmount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, zonedDateTime);
        when(cacheService.getIfPresent(wallet.walletId(), context)).thenReturn(Optional.of(wallet));
        when(cacheService.put(updatedWallet, context)).thenReturn(true);
        when(walletTransactionService.saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest, context).get();

        verify(cacheService).getIfPresent(wallet.walletId(), context);
        verify(cacheService).put(updatedWallet, context);
        verify(walletTransactionService).saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context);
        assertEquals(expectedTransaction, walletTransaction);
    }

    @Test
    void shouldTransferFundsByWithdrawal() {
        Wallet wallet = new Wallet(TRANSACTION_UUID, 100.0);
        double withdrawalAmount = 50.0;
        Wallet updatedWallet = new Wallet(wallet.walletId(), wallet.walletBalance() - withdrawalAmount);
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(100001L).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), withdrawalAmount, TransactionType.WITHDRAWAL);
        WalletTransaction expectedTransaction = new WalletTransaction(TRANSACTION_UUID, updatedWallet.walletId(), withdrawalAmount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, zonedDateTime);
        when(cacheService.getIfPresent(wallet.walletId(), context)).thenReturn(Optional.of(wallet));
        when(cacheService.put(updatedWallet, context)).thenReturn(true);
        when(walletTransactionService.saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest, context).get();

        verify(cacheService).getIfPresent(wallet.walletId(), context);
        verify(cacheService).put(updatedWallet, context);
        verify(walletTransactionService).saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context);
        assertEquals(expectedTransaction, walletTransaction);
    }

    @Test
    void shouldTransferFundsByDepositWhenWalletDoesNotExistInCache() {
        Wallet wallet = new Wallet(TRANSACTION_UUID, 0.0);
        Wallet updatedWallet = new Wallet(wallet.walletId(), wallet.walletBalance() + 50.0);
        double depositAmount = 50.0;
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(100001L).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), depositAmount, TransactionType.DEPOSIT);
        WalletTransaction expectedTransaction = new WalletTransaction(TRANSACTION_UUID, updatedWallet.walletId(), depositAmount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, zonedDateTime);

        when(cacheService.getIfPresent(wallet.walletId(), context)).thenReturn(Optional.empty());
        when(cacheService.addToCache(wallet, context)).thenReturn(Optional.of(wallet));
        when(cacheService.put(updatedWallet, context)).thenReturn(true);
        when(walletTransactionService.saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest, context).get();

        verify(cacheService).getIfPresent(wallet.walletId(), context);
        verify(cacheService).put(updatedWallet, context);
        verify(walletTransactionService).saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context);
        assertEquals(expectedTransaction, walletTransaction);
    }

    @Test
    void shouldNotTransferFundsWhenNewAccountCreationFailure() {
        Wallet wallet = new Wallet(TRANSACTION_UUID, 0.0);
        Wallet updatedWallet = new Wallet(wallet.walletId(), wallet.walletBalance() + 50.0);
        double depositAmount = 50.0;
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(100001L).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), depositAmount, TransactionType.DEPOSIT);
        WalletTransaction expectedTransaction = new WalletTransaction(TRANSACTION_UUID, updatedWallet.walletId(), depositAmount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, zonedDateTime);

        when(cacheService.getIfPresent(wallet.walletId(), context)).thenReturn(Optional.empty());
        when(cacheService.addToCache(wallet, context)).thenReturn(Optional.empty());
        when(walletTransactionService.saveTransaction(fundTransferRequest, TransactionStatus.FAILURE, context)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest, context).get();

        verify(cacheService).getIfPresent(wallet.walletId(), context);
        verify(cacheService).addToCache(wallet, context);
        verifyNoMoreInteractions(cacheService);
        verify(walletTransactionService).saveTransaction(fundTransferRequest, TransactionStatus.FAILURE, context);
        assertEquals(expectedTransaction, walletTransaction);
    }

    @Test
    void shouldNotTransferFundsByDepositWhenWalletUpdateUnsuccessful() {
        Wallet wallet = new Wallet(TRANSACTION_UUID, 0.0);
        Wallet updatedWallet = new Wallet(wallet.walletId(), wallet.walletBalance() + 50.0);
        double depositAmount = 50.0;
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(100001L).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), depositAmount, TransactionType.DEPOSIT);
        WalletTransaction expectedTransaction = new WalletTransaction(TRANSACTION_UUID, updatedWallet.walletId(), depositAmount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, zonedDateTime);

        when(cacheService.getIfPresent(wallet.walletId(), context)).thenReturn(Optional.of(wallet));
        when(cacheService.put(updatedWallet,context )).thenReturn(false);
        when(walletTransactionService.saveTransaction(fundTransferRequest, TransactionStatus.FAILURE, context)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest, context).get();

        verify(cacheService).getIfPresent(wallet.walletId(), context);
        verify(cacheService).put(updatedWallet, context);
        verify(walletTransactionService).saveTransaction(fundTransferRequest, TransactionStatus.FAILURE, context);
        assertEquals(expectedTransaction, walletTransaction);
    }

    @Test
    void shouldRetrieveBalanceByCache() {
        Wallet expectedWallet = new Wallet(TRANSACTION_UUID, 0.0);
        when(cacheService.getIfPresent(expectedWallet.walletId(), context)).thenReturn(Optional.of(expectedWallet));

        Double actualBalance = walletService.retrieveBalance(expectedWallet.walletId(), context).get();

        assertEquals(expectedWallet.walletBalance(), actualBalance);
    }

    @Test
    void shouldGetAllTransactions() {
        Wallet wallet = new Wallet(TRANSACTION_UUID, 100.0);
        List<WalletTransaction> expectedTransactions = List.of(
                new WalletTransaction(UUID.randomUUID().toString(), wallet.walletId(), 100.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, Instant.now().atZone(ZoneId.systemDefault()))
        );
        when(walletTransactionService.getAllTransactionsByWalletId(wallet.walletId(), context)).thenReturn(expectedTransactions);

        List<WalletTransaction> actualWalletTransactions = walletService.getAllTransactions(wallet.walletId(), context);

        assertEquals(expectedTransactions, actualWalletTransactions);
    }
}