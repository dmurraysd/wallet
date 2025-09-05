package com.dmurraysd.spring.wallet.repository;

import jakarta.persistence.*;

import java.util.Objects;


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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WalletEntity that = (WalletEntity) o;
        return Double.compare(walletBalance, that.walletBalance) == 0 && Objects.equals(id, that.id) && Objects.equals(walletId, that.walletId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, walletId, walletBalance);
    }
}
