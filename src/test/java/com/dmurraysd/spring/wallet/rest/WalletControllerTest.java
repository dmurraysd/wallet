package com.dmurraysd.spring.wallet.rest;

import com.dmurraysd.spring.wallet.logging.IdProvider;
import com.dmurraysd.spring.wallet.logging.LoggingUtil;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTest {
    private static final String SOURCE_ID = "wallet-rest-api";
    public static final String CONTEXT_UUID = "a1d1429a-c68d-43e8-ac6d-9d62a1f47c03";
    private static final IdProvider context = LoggingUtil.loggingContext(UUID.fromString(CONTEXT_UUID), SOURCE_ID);

    public static final String ACCOUNT_ID = UUID.randomUUID().toString();
    public static final double OPEN_BALANCE = 100.0;

    private final WalletService walletService = mock(WalletService.class);

    private WalletController walletController;


    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletController = new WalletController(walletService, () -> UUID.fromString(CONTEXT_UUID));
        wallet = new Wallet(ACCOUNT_ID, OPEN_BALANCE);
    }

    @Test
    void shouldCreateNewAccount() {
        when(walletService.createAccount(context)).thenReturn(Optional.of(wallet));

        ResponseEntity<Wallet> actualAccount = walletController.create();

        assertTrue(actualAccount.getStatusCode().is2xxSuccessful());
        assertEquals(wallet, actualAccount.getBody());
        verify(walletService).createAccount(context);
    }

    @Test
    void shouldDepositFunds() {
        double depositAmount = 50.0;
        WalletTransaction expectedTransaction = new WalletTransaction(UUID.randomUUID().toString(), wallet.walletId(), 150.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, Instant.now().atZone(ZoneId.systemDefault()));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), depositAmount, TransactionType.DEPOSIT);
        when(walletService.transferFunds(fundTransferRequest, context)).thenReturn(Optional.of(expectedTransaction));

        ResponseEntity<WalletTransaction> depositTransaction = walletController.fundTransfer(fundTransferRequest);

        assertTrue(depositTransaction.getStatusCode().is2xxSuccessful());
        verify(walletService).transferFunds(fundTransferRequest, context);
        assertEquals(expectedTransaction, depositTransaction.getBody());
    }

    @Test
    void shouldWithdrawFunds() {
        double withdrawalAmount = 50.0;
        WalletTransaction expectedTransaction = new WalletTransaction(UUID.randomUUID().toString(), wallet.walletId(), 50.0, TransactionType.WITHDRAWAL, TransactionStatus.SUCCESS, Instant.now().atZone(ZoneId.systemDefault()));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), withdrawalAmount, TransactionType.WITHDRAWAL);
        when(walletService.transferFunds(fundTransferRequest, context)).thenReturn(Optional.of(expectedTransaction));

        ResponseEntity<WalletTransaction> withdrawalTransaction = walletController.fundTransfer(fundTransferRequest);

        assertTrue(withdrawalTransaction.getStatusCode().is2xxSuccessful());
        verify(walletService).transferFunds(fundTransferRequest, context);
        assertEquals(expectedTransaction, withdrawalTransaction.getBody());
    }

    @Test
    void shouldRetrieveAccountBalance() {
        double expectedBalance = 100.0;
        String walletId = UUID.randomUUID().toString();
        when(walletService.retrieveBalance(walletId, context)).thenReturn(Optional.of(expectedBalance));

        ResponseEntity<Double> expectedAccountBalance = walletController.getBalance(walletId);

        verify(walletService).retrieveBalance(walletId, context);
        assertTrue(expectedAccountBalance.getStatusCode().is2xxSuccessful());
        assertEquals(expectedBalance, expectedAccountBalance.getBody());
    }

    @Test
    void shouldNotRetrieveAccountBalanceWhenInvalid() {
        double invalidBalance = -1.0;
        when(walletService.retrieveBalance(wallet.walletId(), context)).thenReturn(Optional.of(invalidBalance));

        ResponseEntity<Double> balanceResponse = walletController.getBalance(wallet.walletId());

        verify(walletService).retrieveBalance(wallet.walletId(), context);
        assertTrue(balanceResponse.getStatusCode().is4xxClientError());
        assertNull(balanceResponse.getBody());
    }

    @Test
    void shouldListAnAccountTransactions() {
        List<WalletTransaction> expectedTransactions = List.of(
                new WalletTransaction(UUID.randomUUID().toString(), wallet.walletId(), 100.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, Instant.now().atZone(ZoneId.systemDefault()))
        );
        when(walletService.getAllTransactions(wallet.walletId(), context)).thenReturn(expectedTransactions);

        ResponseEntity<List<WalletTransaction>> actualTransactions = walletController.getWalletTransactions(wallet.walletId());

        assertTrue(actualTransactions.getStatusCode().is2xxSuccessful());
        verify(walletService).getAllTransactions(wallet.walletId(), context);
        assertEquals(expectedTransactions, actualTransactions.getBody());
    }

    @Test
    void shouldThrowValidationExceptionWhenInvalidFundTransferRequest() throws JsonProcessingException {
        final MockMvcTester mockMvc = MockMvcTester.of(walletController);

        assertThat(mockMvc.post().uri("/v1/wallet/transfer")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(new ObjectMapper().writeValueAsString(new FundTransferRequest("", 100.0, TransactionType.DEPOSIT)))
        )
                .hasStatus4xxClientError()
                .failure().hasMessageContaining("Invalid walletId : Wallet ID is empty");
    }
}
