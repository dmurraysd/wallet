package com.dmurraysd.spring.wallet.rest;


import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Wallet> create() {
        return walletService.createAccount()
                .map(walletAccount -> ResponseEntity.status(HttpStatus.CREATED).body(walletAccount))
                .orElse(ResponseEntity.internalServerError().build());
    }

    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WalletTransaction> fundTransfer(@Valid @RequestBody FundTransferRequest fundTransferRequest) {
        return walletService.transferFunds(fundTransferRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.internalServerError().build());
    }

    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Double> getBalance(@NotBlank @RequestBody String walletId) {
        return walletService.retrieveBalance(walletId)
                .filter(amount -> amount > 0)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WalletTransaction>> getWalletTransactions(@NotBlank @RequestBody String walletId) {
        return ResponseEntity.ok(walletService.getAllTransactions(walletId));
    }
}
