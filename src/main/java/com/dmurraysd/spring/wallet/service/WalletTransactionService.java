package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.repository.WalletTransactionRepository;
import com.dmurraysd.spring.wallet.repository.WalletTransactionEntityMapper;
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
public class WalletTransactionService {

    private final WalletTransactionRepository transactionRepository;
    private final Supplier<UUID> uuidSupplier;
    private final Supplier<Long> timestampSupplier;

    public WalletTransactionService(WalletTransactionRepository transactionRepository,
                                    Supplier<UUID> uuidSupplier,
                                    Supplier<Long> timestampSupplier) {
        this.transactionRepository = transactionRepository;
        this.uuidSupplier = uuidSupplier;
        this.timestampSupplier = timestampSupplier;
    }

    public Optional<WalletTransaction> saveTransaction(String walletId, FundTransferRequest fundTransferRequest, TransactionStatus transactionStatus) {
        String transactionId = uuidSupplier.get().toString();
        ZonedDateTime transactionTimestamp = Instant.ofEpochMilli(timestampSupplier.get()).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));

        WalletTransaction walletTransaction =
                new WalletTransaction(transactionId, walletId, fundTransferRequest.amount(), fundTransferRequest.transactionType(), transactionStatus, transactionTimestamp);
        return Optional.of(transactionRepository.save(WalletTransactionEntityMapper.toEntity(walletTransaction)))
                .map(WalletTransactionEntityMapper::toDTO);
    }

    public Optional<WalletTransaction> saveTransaction(FundTransferRequest fundTransferRequest, TransactionStatus transactionStatus) {
        return saveTransaction(null, fundTransferRequest, transactionStatus);
    }

    public List<WalletTransaction> getAllTransactionsByWalletId(String walletId) {
        return transactionRepository.findByWalletId(walletId).stream()
                .map(WalletTransactionEntityMapper::toDTO).toList();
    }
}
