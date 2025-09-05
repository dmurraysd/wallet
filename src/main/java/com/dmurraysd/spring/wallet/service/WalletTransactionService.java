package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.repository.WalletTransactionRepository;
import com.dmurraysd.spring.wallet.repository.WalletTransactionEntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dmurraysd.spring.wallet.logging.LoggingUtil.formatLogMessage;

@Component
public class WalletTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(WalletTransactionService.class);

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

    public Optional<WalletTransaction> saveTransaction(String walletId, FundTransferRequest fundTransferRequest, TransactionStatus transactionStatus, IdProvider context) {
        String transactionId = uuidSupplier.get().toString();
        logger.info(formatLogMessage(context, "Saving transaction with id [%s] wallet with Id [%s]", transactionId, walletId));

        ZonedDateTime transactionTimestamp = Instant.ofEpochMilli(timestampSupplier.get()).truncatedTo(ChronoUnit.MILLIS).atZone(ZoneId.of("Z"));

        WalletTransaction walletTransaction =
                new WalletTransaction(transactionId, walletId, fundTransferRequest.amount(), fundTransferRequest.transactionType(), transactionStatus, transactionTimestamp);
        return Optional.of(transactionRepository.save(WalletTransactionEntityMapper.toEntity(walletTransaction)))
                .map(WalletTransactionEntityMapper::toDTO);
    }

    public Optional<WalletTransaction> saveTransaction(FundTransferRequest fundTransferRequest, TransactionStatus transactionStatus, IdProvider context) {
        return saveTransaction(null, fundTransferRequest, transactionStatus, context);
    }

    public List<WalletTransaction> getAllTransactionsByWalletId(String walletId, IdProvider context) {
        logger.info(formatLogMessage(context, "Retrieving all transactions wallet with Id [%s]", walletId));
        return transactionRepository.findByWalletId(walletId).stream()
                .map(WalletTransactionEntityMapper::toDTO).toList();
    }
}
