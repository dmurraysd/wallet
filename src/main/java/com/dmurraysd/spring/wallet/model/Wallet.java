package com.dmurraysd.spring.wallet.model;

import java.io.Serializable;

public record Wallet(String walletId, Double walletBalance) implements Serializable {
}
