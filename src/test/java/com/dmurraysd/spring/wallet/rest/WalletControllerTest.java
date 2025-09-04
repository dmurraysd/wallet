package com.dmurraysd.spring.wallet.rest;

import com.dmurraysd.spring.wallet.exception.WalletError;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTest {

    public static final String ACCOUNT_ID = UUID.randomUUID().toString();
    public static final double OPEN_BALANCE = 100.0;

    private static final WalletService walletService = mock(WalletService.class);

    private WalletController walletController;


    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletController = new WalletController(walletService);
        wallet = new Wallet(ACCOUNT_ID, OPEN_BALANCE);
    }

    @Test
    void shouldCreateNewAccount() {
        when(walletService.createAccount()).thenReturn(wallet);

        ResponseEntity<Wallet> actualAccount = walletController.create();

        assertTrue(actualAccount.getStatusCode().is2xxSuccessful());
        assertEquals(wallet, actualAccount.getBody());
        verify(walletService).createAccount();
    }

    @Test
    void shouldDepositFunds() {
        double depositAmount = 50.0;
        WalletTransaction expectedTransaction = new WalletTransaction(UUID.randomUUID().toString(), wallet, 150.0, TransactionType.DEPOSIT, Instant.now().atZone(ZoneId.systemDefault()));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.accountId(), depositAmount, TransactionType.DEPOSIT);
        when(walletService.transferFunds(fundTransferRequest)).thenReturn(expectedTransaction);

        ResponseEntity<WalletTransaction> depositTransaction = walletController.fundTransfer(fundTransferRequest);

        assertTrue(depositTransaction.getStatusCode().is2xxSuccessful());
        verify(walletService).transferFunds(fundTransferRequest);
        assertEquals(expectedTransaction, depositTransaction.getBody());
    }

    @Test
    void shouldWithdrawFunds() {
        double withdrawalAmount = 50.0;
        WalletTransaction expectedTransaction = new WalletTransaction(UUID.randomUUID().toString(), wallet, 50.0, TransactionType.WITHDRAWAL, Instant.now().atZone(ZoneId.systemDefault()));
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.accountId(), withdrawalAmount, TransactionType.WITHDRAWAL);
        when(walletService.transferFunds(fundTransferRequest)).thenReturn(expectedTransaction);

        ResponseEntity<WalletTransaction> withdrawalTransaction = walletController.fundTransfer(fundTransferRequest);

        assertTrue(withdrawalTransaction.getStatusCode().is2xxSuccessful());
        verify(walletService).transferFunds(fundTransferRequest);
        assertEquals(expectedTransaction, withdrawalTransaction.getBody());
    }

    @Test
    void shouldRetrieveAccountBalance() {
        double expectedBalance = 100.0;
        when(walletService.retrieveBalance(wallet.accountId())).thenReturn(expectedBalance);

        ResponseEntity<Double> expectedAccountBalance = walletController.getBalance(wallet.accountId());

        assertTrue(expectedAccountBalance.getStatusCode().is2xxSuccessful());
        verify(walletService).retrieveBalance(wallet.accountId());
        assertEquals(expectedBalance, expectedAccountBalance.getBody());
    }

    @Test
    void shouldListAnAccountTransactions() {
        List<WalletTransaction> expectedTransactions = List.of(
                new WalletTransaction(UUID.randomUUID().toString(), wallet, 100.0, TransactionType.DEPOSIT, Instant.now().atZone(ZoneId.systemDefault()))
        );
        when(walletService.getAllTransactions(wallet.accountId())).thenReturn(expectedTransactions);

        ResponseEntity<List<WalletTransaction>> actualTransactions = walletController.getWalletTransactions(wallet.accountId());

        assertTrue(actualTransactions.getStatusCode().is2xxSuccessful());
        verify(walletService).getAllTransactions(wallet.accountId());
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
