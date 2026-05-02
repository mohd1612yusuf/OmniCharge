package com.omnicharge.paymentservice.dto;

public class RazorpayOrderRequest {
    private Long amount;      // in paise (e.g., ₹99 = 9900)
    private String currency;  // "INR"
    private String receipt;   // unique receipt id (e.g., recharge-123)

    // Getters and Setters
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getReceipt() { return receipt; }
    public void setReceipt(String receipt) { this.receipt = receipt; }
}
