package com.omnicharge.paymentservice.controller;

import com.omnicharge.paymentservice.model.Transaction;
import com.omnicharge.paymentservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(transactionRepository.findByUserId(userId));
    }
}
