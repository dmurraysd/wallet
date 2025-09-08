package com.dmurraysd.spring.wallet.repository;

import com.dmurraysd.spring.wallet.model.Wallet;

public class WalletEntityMapper {
    private WalletEntityMapper() {
    }

    public static Wallet toDTO(WalletEntity walletEntity) {
        return new Wallet(walletEntity.getWalletId(), walletEntity.getWalletBalance());
    }

    public static WalletEntity toEntity(Wallet wallet) {
        return new WalletEntity(wallet.walletId(), wallet.walletBalance());
    }

    public static WalletEntity toUpdatedBalanceEntity(Wallet wallet, WalletEntity walletEntity) {
        walletEntity.setWalletBalance(wallet.walletBalance());
        return walletEntity;
    }
}
