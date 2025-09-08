package com.dmurraysd.spring.wallet;

import com.dmurraysd.spring.wallet.config.RedisTestConfig;
import com.dmurraysd.spring.wallet.config.WalletTestConfig;
import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.model.transaction.FundTransferRequest;
import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import com.dmurraysd.spring.wallet.model.transaction.WalletTransaction;
import com.dmurraysd.spring.wallet.repository.WalletEntityMapper;
import com.dmurraysd.spring.wallet.repository.WalletRepository;
import com.dmurraysd.spring.wallet.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static com.dmurraysd.spring.wallet.config.WalletTestConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(classes = {RedisTestConfig.class, WalletTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.data.redis.port=6385",
                "spring.data.redis.host=localhost",
                "lock.expiry.in.mills=1000"})
class WalletApplicationTests {

    @LocalServerPort
    private Integer serverPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    WalletRepository walletRepository;

    @Test
    void shouldCreateAccountAndRetrieveAccountBalance() {
        final String walletId = SUPPLIED_TEST_UUID;
        final double expectedBalance = 0.0;
        final String baseUrl = String.format("http://localhost:%s/", serverPort);
        URI uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet").build().toUri();
        Wallet createdWallet = new Wallet(walletId, expectedBalance);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<Wallet> walletResponse = testRestTemplate.exchange(uri, HttpMethod.POST, null, Wallet.class);

        assertTrue(walletResponse.getStatusCode().is2xxSuccessful());
        assertEquals(createdWallet, walletResponse.getBody());

        uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet/balance/" + walletId).build().toUri();

        ResponseEntity<String> balanceResponse = testRestTemplate.exchange(uri, HttpMethod.GET, null, String.class);

        assertTrue(balanceResponse.getStatusCode().is2xxSuccessful());
        assertEquals(expectedBalance, Double.parseDouble(balanceResponse.getBody()));
    }

    @Test
    void shouldTransferFundsWithDeposit() {
        Instant transactionTimestamp = Instant.parse(INSTANT_TIMESTAMP);
        Wallet wallet = new Wallet(TEST_WALLET_UUID_2, 100.0);
        walletRepository.save(WalletEntityMapper.toEntity(wallet));
        final String baseUrl = String.format("http://localhost:%s/", serverPort);
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), 50.0, TransactionType.DEPOSIT);

        URI uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet/transfer").build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<FundTransferRequest> httpEntity = new HttpEntity<>(fundTransferRequest, headers);
        WalletTransaction expectedWalletTransaction = new WalletTransaction(SUPPLIED_TEST_UUID, wallet.walletId(), 50.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, transactionTimestamp);

        ResponseEntity<String> walletResponse = testRestTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class);
        WalletTransaction actualWalletTransaction = TestUtils.deSerialize(walletResponse.getBody(), WalletTransaction.class);

        assertTrue(walletResponse.getStatusCode().is2xxSuccessful());
        assertEquals(expectedWalletTransaction, actualWalletTransaction);
    }

    @Test
    void shouldListAnAccountTransactions() {
        Instant transactionTimestamp = Instant.parse(INSTANT_TIMESTAMP);
        Wallet wallet = new Wallet(TEST_WALLET_UUID_3, 100.0);
        walletRepository.save(WalletEntityMapper.toEntity(wallet));
        final String baseUrl = String.format("http://localhost:%s/", serverPort);
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), 50.0, TransactionType.DEPOSIT);

        URI uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet/transfer").build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<FundTransferRequest> httpEntity = new HttpEntity<>(fundTransferRequest, headers);
        WalletTransaction expectedWalletTransaction = new WalletTransaction(SUPPLIED_TEST_UUID, wallet.walletId(), 50.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, transactionTimestamp);

        testRestTemplate.exchange(uri, HttpMethod.POST, httpEntity, WalletTransaction.class);

        URI uriForTransactions = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet/transactions/" + TEST_WALLET_UUID_3).build().toUri();
        HttpEntity<String> httpEntityForTransactions = new HttpEntity<>(headers);
        ResponseEntity<List<WalletTransaction>> walletTransactionsResponse = testRestTemplate.exchange(uriForTransactions, HttpMethod.GET, httpEntityForTransactions, new ParameterizedTypeReference<>() {
        });

        assertTrue(walletTransactionsResponse.getStatusCode().is2xxSuccessful());
        assertEquals(List.of(expectedWalletTransaction), walletTransactionsResponse.getBody());
    }

    @Test
    void shouldCreateNewAccountWhenTransactionOccurs() {
        Instant transactionTimestamp = Instant.parse(INSTANT_TIMESTAMP);
        Wallet wallet = new Wallet(TEST_WALLET_UUID_4, 100.0);
        final String baseUrl = String.format("http://localhost:%s/", serverPort);
        FundTransferRequest fundTransferRequest = new FundTransferRequest(wallet.walletId(), 50.0, TransactionType.DEPOSIT);

        URI uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet/transfer").build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<FundTransferRequest> httpEntity = new HttpEntity<>(fundTransferRequest, headers);
        WalletTransaction expectedWalletTransaction = new WalletTransaction(SUPPLIED_TEST_UUID, wallet.walletId(), 50.0, TransactionType.DEPOSIT, TransactionStatus.SUCCESS, transactionTimestamp);

        ResponseEntity<String> walletResponse = testRestTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class);
        WalletTransaction actualWalletTransaction = TestUtils.deSerialize(walletResponse.getBody(), WalletTransaction.class);

        assertTrue(walletResponse.getStatusCode().is2xxSuccessful());
        assertEquals(expectedWalletTransaction, actualWalletTransaction);

        uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet/balance/" + SUPPLIED_TEST_UUID).build().toUri();

        final double expectedBalance = 50.0;

        ResponseEntity<String> balanceResponse = testRestTemplate.exchange(uri, HttpMethod.GET, null, String.class);

        assertTrue(balanceResponse.getStatusCode().is2xxSuccessful());
        assertEquals(expectedBalance, Double.parseDouble(balanceResponse.getBody()));
    }

}
