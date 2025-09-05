package com.dmurraysd.spring.wallet.service;

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
        when(cacheService.addToCache(expectedWallet)).thenReturn(Optional.of(expectedWallet));

        Wallet actualWallet = walletService.createAccount().get();

        verify(cacheService).addToCache(expectedWallet);
        assertEquals(expectedWallet, actualWallet);
    }

    @Test
    void shouldCreateAccountWhenTransactionOccurs() {
        Wallet expectedWallet = new Wallet(TRANSACTION_UUID, 0.0);
        when(cacheService.addToCache(expectedWallet)).thenReturn(Optional.of(expectedWallet));

        Wallet actualWallet = walletService.createAccount(expectedWallet.walletId(), expectedWallet.walletBalance()).orElse(null);

        verify(cacheService).addToCache(expectedWallet);
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
        when(cacheService.getIfPresent(wallet.walletId())).thenReturn(Optional.of(wallet));
        when(cacheService.put(updatedWallet)).thenReturn(true);
        when(walletTransactionService.saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest).get();

        verify(cacheService).getIfPresent(wallet.walletId());
        verify(cacheService).put(updatedWallet);
        verify(walletTransactionService).saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS);
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

        when(cacheService.getIfPresent(wallet.walletId())).thenReturn(Optional.empty());
        when(cacheService.addToCache(wallet)).thenReturn(Optional.of(wallet));
        when(cacheService.put(updatedWallet)).thenReturn(true);
        when(walletTransactionService.saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest).get();

        verify(cacheService).getIfPresent(wallet.walletId());
        verify(cacheService).put(updatedWallet);
        verify(walletTransactionService).saveTransaction(updatedWallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS);
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

        when(cacheService.getIfPresent(wallet.walletId())).thenReturn(Optional.empty());
        when(cacheService.addToCache(wallet)).thenReturn(Optional.empty());
        when(walletTransactionService.saveTransaction(fundTransferRequest, TransactionStatus.FAILURE)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest).get();

        verify(cacheService).getIfPresent(wallet.walletId());
        verify(cacheService).addToCache(wallet);
        verifyNoMoreInteractions(cacheService);
        verify(walletTransactionService).saveTransaction(fundTransferRequest, TransactionStatus.FAILURE);
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

        when(cacheService.getIfPresent(wallet.walletId())).thenReturn(Optional.of(wallet));
        when(cacheService.put(updatedWallet)).thenReturn(false);
        when(walletTransactionService.saveTransaction(fundTransferRequest, TransactionStatus.FAILURE)).thenReturn(Optional.of(expectedTransaction));

        WalletTransaction walletTransaction = walletService.transferFunds(fundTransferRequest).get();

        verify(cacheService).getIfPresent(wallet.walletId());
        verify(cacheService).put(updatedWallet);
        verify(walletTransactionService).saveTransaction(fundTransferRequest, TransactionStatus.FAILURE);
        assertEquals(expectedTransaction, walletTransaction);
    }

    @Test
    void shouldRetrieveBalanceByCache() {
        Wallet expectedWallet = new Wallet(TRANSACTION_UUID, 0.0);
        when(cacheService.getIfPresent(expectedWallet.walletId())).thenReturn(Optional.of(expectedWallet));

        Double actualBalance = walletService.retrieveBalance(expectedWallet.walletId()).get();

        assertEquals(expectedWallet.walletBalance(), actualBalance);
    }

    @Test
    void shouldGetAllTransactions() {
        Wallet wallet = new Wallet(TRANSACTION_UUID, 100.0);
        List<WalletTransaction> expectedTransactions = List.of(
                new WalletTransaction(UUID.randomUUID().toString(), wallet.walletId(), 100.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, Instant.now().atZone(ZoneId.systemDefault()))
        );
        when(walletTransactionService.getAllTransactionsByWalletId(wallet.walletId())).thenReturn(expectedTransactions);

        List<WalletTransaction> actualWalletTransactions = walletService.getAllTransactions(wallet.walletId());

        assertEquals(expectedTransactions, actualWalletTransactions);
    }
}