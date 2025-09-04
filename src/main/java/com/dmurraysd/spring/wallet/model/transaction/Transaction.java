package com.dmurraysd.spring.wallet.model.transaction;

import java.time.ZonedDateTime;

public interface Transaction {
    String transactionId();
    TransactionType transactionType();
    double amount();
    ZonedDateTime zonedDateTime();
}
