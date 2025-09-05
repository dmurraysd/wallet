package com.dmurraysd.spring.wallet;

import com.dmurraysd.spring.wallet.util.config.RedisTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = {RedisTestConfig.class})
class WalletApplicationTests {

    @Test
    void shouldRetrieveAccountBalance() {
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
