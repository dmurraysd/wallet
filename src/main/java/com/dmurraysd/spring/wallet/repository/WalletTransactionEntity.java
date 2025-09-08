package com.dmurraysd.spring.wallet.repository;

import com.dmurraysd.spring.wallet.model.transaction.TransactionStatus;
import com.dmurraysd.spring.wallet.model.transaction.TransactionType;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "wallet_transaction")
public class WalletTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transactionId")
    private String transactionId;

    @Column(name = "walletId")
    private String walletId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "transactionType")
    private TransactionType transactionType;

    @Column(name = "transactionStatus")
    private TransactionStatus transactionStatus;

    @Column(name = "transactionTimestamp")
    private Long transactionTimestamp;

    public WalletTransactionEntity() {
    }

    public WalletTransactionEntity(String transactionId, String walletId, Double amount, TransactionType transactionType, TransactionStatus transactionStatus, Long transactionTimestamp) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.transactionTimestamp = transactionTimestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getWalletId() {
        return this.walletId;
    }

    public void setWalletId(String wallet) {
        this.walletId = wallet;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Long getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(Long transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WalletTransactionEntity that = (WalletTransactionEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(transactionId, that.transactionId) && Objects.equals(walletId, that.walletId) && Objects.equals(amount, that.amount) && transactionType == that.transactionType && transactionStatus == that.transactionStatus && Objects.equals(transactionTimestamp, that.transactionTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionId, walletId, amount, transactionType, transactionStatus, transactionTimestamp);
    }

    @Override
    public String toString() {
        return "WalletTransactionEntity{" +
                "transactionId='" + transactionId + '\'' +
                ", walletId='" + walletId + '\'' +
                ", amount=" + amount +
                ", transactionType=" + transactionType +
                ", transactionStatus=" + transactionStatus +
                ", transactionTimestamp=" + transactionTimestamp +
                '}';
    }
}

