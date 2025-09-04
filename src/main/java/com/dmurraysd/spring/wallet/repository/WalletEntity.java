package com.dmurraysd.spring.wallet.repository;

import jakarta.persistence.*;


@Entity
@Table(name = "wallet")
public class WalletEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "walletId")
    private String walletId;

    @Column(name = "walletBalance")
    private double walletBalance;


    public WalletEntity() {
    }

    public WalletEntity(String walletId, double walletBalance) {
        this.walletId = walletId;
        this.walletBalance = walletBalance;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double balance) {
        this.walletBalance = balance;
    }
}
