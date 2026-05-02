package com.omnicharge.paymentservice.controller;

import com.omnicharge.paymentservice.model.Transaction;
import com.omnicharge.paymentservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.omnicharge.paymentservice.service.RazorpayService;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RazorpayService razorpayService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(transactionRepository.findByUserId(userId));
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(@RequestBody Map<String, Object> request) {
        try {
            long amount = Long.parseLong(request.get("amount").toString()); // amount in paise
            String receipt = "recharge-" + System.currentTimeMillis();

            JSONObject order = razorpayService.createOrder(amount, receipt);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getString("id"));
            response.put("amount", order.getLong("amount"));
            response.put("currency", order.getString("currency"));
            response.put("keyId", "rzp_test_Sjzre7mbZL1LZj"); // Safe to send to frontend

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
