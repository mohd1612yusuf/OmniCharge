package com.omnicharge.rechargeservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recharges")
public class RechargeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String mobileNumber;
    private Long operatorId;
    private Long planId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime requestTime;
    private LocalDateTime completionTime;

    public RechargeRecord() {}

    public RechargeRecord(Long userId, String mobileNumber, Long operatorId, Long planId, BigDecimal amount) {
        this.userId = userId;
        this.mobileNumber = mobileNumber;
        this.operatorId = operatorId;
        this.planId = planId;
        this.amount = amount;
        this.status = "PENDING";
        this.requestTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    public LocalDateTime getCompletionTime() { return completionTime; }
    public void setCompletionTime(LocalDateTime completionTime) { this.completionTime = completionTime; }
}
