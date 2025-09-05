package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.logging.LoggingUtil;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.repository.WalletTransactionRepository;
import com.dmurraysd.spring.wallet.repository.WalletTransactionEntityMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WalletTransactionServiceTest {
    private static final String SOURCE_ID = "wallet-rest-api";
    public static final String CONTEXT_UUID = "a1d1429a-c68d-43e8-ac6d-9d62a1f47c03";
    private static final IdProvider context = LoggingUtil.loggingContext(UUID.fromString(CONTEXT_UUID), SOURCE_ID);

    public static final String SUPPLIED_UUID = "00000000-0000-0000-0000-000000000000";
    public static final long TIMESTAMP = 100000L;
    private final WalletTransactionRepository transactionRepository = mock(WalletTransactionRepository.class);
    private final WalletTransactionService walletTransactionService = new WalletTransactionService(transactionRepository, () -> UUID.fromString(SUPPLIED_UUID), () -> TIMESTAMP);

    @Test
    void shouldSaveTransaction() {
        ZonedDateTime transactionTimestamp = Instant.ofEpochMilli(TIMESTAMP).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));
        Wallet wallet = new Wallet("123", 100.0);
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), 50.0, TransactionType.DEPOSIT);
        WalletTransaction expectedTransaction = new WalletTransaction(SUPPLIED_UUID, wallet.walletId(), 50.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, transactionTimestamp);
        when(transactionRepository.save(any())).thenReturn(WalletTransactionEntityMapper.toEntity(expectedTransaction));

        WalletTransaction walletTransaction = walletTransactionService.saveTransaction(wallet.walletId(), fundTransferRequest, TransactionStatus.SUCCESS, context).get();

        verify(transactionRepository).save(any());
        assertEquals(expectedTransaction, walletTransaction);
    }

    @Test
    void shouldGetAllTransactionsByWalletId() {
        ZonedDateTime transactionTimestamp = Instant.ofEpochMilli(TIMESTAMP).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));
        Wallet wallet = new Wallet("123", 100.0);
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), 50.0, TransactionType.DEPOSIT);
        WalletTransaction expectedTransaction  = new WalletTransaction(SUPPLIED_UUID, wallet.walletId(), fundTransferRequest.amount(), fundTransferRequest.transactionType(), TransactionStatus.SUCCESS, transactionTimestamp);

        when(transactionRepository.findByWalletId(any())).thenReturn(List.of(WalletTransactionEntityMapper.toEntity(expectedTransaction)));

        List<WalletTransaction> walletTransaction = walletTransactionService.getAllTransactionsByWalletId(wallet.walletId(), context);

        verify(transactionRepository).findByWalletId(any());
        assertEquals(List.of(expectedTransaction), walletTransaction);
    }
}