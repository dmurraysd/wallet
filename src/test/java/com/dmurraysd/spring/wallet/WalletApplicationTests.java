package com.dmurraysd.spring.wallet;

import com.dmurraysd.spring.wallet.model.Wallet;
import com.dmurraysd.spring.wallet.util.config.RedisTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.dmurraysd.spring.wallet.util.config.WalletTestConfig.TEST_UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {RedisTestConfig.class})
class WalletApplicationTests {

    @LocalServerPort
    private Integer serverPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldRetrieveAccountBalance() {


        final String walletId = TEST_UUID;
        final double expectedBalance = 50.0;
        final String baseUrl = String.format("http://localhost:%s/", serverPort);
         URI uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet").build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        Wallet wallet = new Wallet(walletId, expectedBalance);
        HttpEntity<Wallet> createEntity = new HttpEntity<>(wallet, headers);

        ResponseEntity<String> walletResponse = testRestTemplate.exchange(uri, HttpMethod.POST, createEntity, String.class);

        assertTrue(walletResponse.getStatusCode().is2xxSuccessful());
        //assertEquals(wallet, response.getBody());
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        uri = UriComponentsBuilder.fromUri(URI.create(baseUrl)).path("v1/wallet/balance").build().toUri();

        HttpEntity<String> entityWithBalance = new HttpEntity<>(walletId, headers);

        ResponseEntity<String> balanceRresponse = testRestTemplate.exchange(uri, HttpMethod.GET, entityWithBalance, String.class);
        System.out.println(balanceRresponse.getStatusCode());
        assertTrue(balanceRresponse.getStatusCode().is2xxSuccessful());
        assertEquals(expectedBalance, Double.parseDouble(balanceRresponse.getBody()));
    }

    @Test
    void shouldTransferFundsBetweenAccounts() {

    }

    @Test
    void shouldListAnAccountTransactions() {

    }

    @Test
    void shouldCreateNewAccountWhenTransactionOccurs() {

    }

    @Test
    void shouldCreateNewAccount() {

    }

}
