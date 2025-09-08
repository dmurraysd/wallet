package com.dmurraysd.spring.wallet.service;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.repository.WalletTransactionEntityMapper;
import com.dmurraysd.spring.wallet.repository.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dmurraysd.spring.wallet.logging.LoggingUtil.formatLogMessage;

@Component
public class WalletTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(WalletTransactionService.class);

    private final WalletTransactionRepository walletTransactionRepository;
    private final Supplier<UUID> uuidSupplier;
    private final Supplier<Instant> instantSupplier;
    private final Supplier<Long> timesStampSupplier;

    public WalletTransactionService(WalletTransactionRepository walletTransactionRepository,
                                    Supplier<UUID> uuidSupplier,
                                    Supplier<Instant> instantSupplier, Supplier<Long> timesStampSupplier) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.uuidSupplier = uuidSupplier;
        this.instantSupplier = instantSupplier;
        this.timesStampSupplier = timesStampSupplier;
    }

    public Optional<WalletTransaction> saveTransaction(String walletId, FundTransferRequest fundTransferRequest, TransactionStatus transactionStatus, IdProvider context) {
        String transactionId = uuidSupplier.get().toString();
        Instant transactionTimestamp = instantSupplier.get().truncatedTo(ChronoUnit.MILLIS);

        logger.info(formatLogMessage(context, "Saving transaction with id [%s] wallet with Id [%s]", transactionId, walletId));

        WalletTransaction walletTransaction =
                new WalletTransaction(transactionId, walletId, fundTransferRequest.amount(), fundTransferRequest.transactionType(), transactionStatus, transactionTimestamp);
        return Optional.of(walletTransactionRepository.save(WalletTransactionEntityMapper.toEntity(walletTransaction, timesStampSupplier)))
                .map(entity -> WalletTransactionEntityMapper.toDTO(entity, instantSupplier));
    }

    public Optional<WalletTransaction> saveTransaction(FundTransferRequest fundTransferRequest, TransactionStatus transactionStatus, IdProvider context) {
        return saveTransaction(null, fundTransferRequest, transactionStatus, context);
    }

    public List<WalletTransaction> getAllTransactionsByWalletId(String walletId, IdProvider context) {
        logger.info(formatLogMessage(context, "Retrieving all transactions for wallet with Id [%s]", walletId));
        return walletTransactionRepository.findByWalletId(walletId)
                .stream()
                .map(entity -> WalletTransactionEntityMapper.toDTO(entity, instantSupplier))
                .toList();
    }
}
