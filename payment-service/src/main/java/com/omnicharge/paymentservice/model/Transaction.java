package com.omnicharge.paymentservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rechargeId;
    private Long userId;
    private BigDecimal amount;
    private String paymentStatus;
    private String referenceNumber;
    private LocalDateTime transactionDate;

    public Transaction() {}

    public Transaction(Long rechargeId, Long userId, BigDecimal amount, String paymentStatus, String referenceNumber) {
        this.rechargeId = rechargeId;
        this.userId = userId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.referenceNumber = referenceNumber;
        this.transactionDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}
