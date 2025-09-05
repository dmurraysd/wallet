package com.dmurraysd.spring.wallet.rest;


import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.logging.LoggingUtil;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dmurraysd.spring.wallet.logging.LoggingUtil.formatLogMessage;

@RestController
@RequestMapping("/v1/wallet")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);
    private static final String SOURCE_ID = "wallet-rest-api";

    private final WalletService walletService;
    private final Supplier<UUID> uuidSupplier;

    public WalletController(WalletService walletService,
                            Supplier<UUID> uuidSupplier) {
        this.walletService = walletService;
        this.uuidSupplier = uuidSupplier;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Wallet> create() {
        IdProvider context = LoggingUtil.loggingContext(uuidSupplier.get(), SOURCE_ID);
        logger.info(formatLogMessage(context, "Adding new wallet"));
        return walletService.createAccount(context)
                .map(walletAccount -> ResponseEntity.status(HttpStatus.CREATED).body(walletAccount))
                .orElse(ResponseEntity.internalServerError().build());
    }

    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WalletTransaction> fundTransfer(@Valid @RequestBody FundTransferRequest fundTransferRequest) {
        IdProvider context = LoggingUtil.loggingContext(uuidSupplier.get(), SOURCE_ID);
        logger.info(formatLogMessage(context, "Transfer of funds with wallet Id %s- [%s]", fundTransferRequest.walletId(), fundTransferRequest.transactionType()));
        return walletService.transferFunds(fundTransferRequest, context)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.internalServerError().build());
    }

    @GetMapping(value = "/balance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Double> getBalance(@NotBlank @RequestBody String walletId) {
        IdProvider context = LoggingUtil.loggingContext(uuidSupplier.get(), SOURCE_ID);
        logger.info(formatLogMessage(context, "Balance retrieval with wallet Id %s", walletId));

        return walletService.retrieveBalance(walletId, context)
                .filter(amount -> amount > 0)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WalletTransaction>> getWalletTransactions(@NotBlank @RequestBody String walletId) {
        IdProvider context = LoggingUtil.loggingContext(uuidSupplier.get(), SOURCE_ID);
        logger.info(formatLogMessage(context, "Retrieval of transactions with wallet Id %s", walletId));
        return ResponseEntity.ok(walletService.getAllTransactions(walletId, context));
    }
}
