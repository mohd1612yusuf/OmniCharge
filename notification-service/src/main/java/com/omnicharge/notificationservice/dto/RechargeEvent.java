package com.omnicharge.notificationservice.dto;

import java.math.BigDecimal;

public class RechargeEvent {
    private Long transactionId;
    private Long userId;
    private String mobileNumber;
    private BigDecimal amount;
    private String status;

    public RechargeEvent() {}

    public RechargeEvent(Long transactionId, Long userId, String mobileNumber, BigDecimal amount, String status) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.mobileNumber = mobileNumber;
        this.amount = amount;
        this.status = status;
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
