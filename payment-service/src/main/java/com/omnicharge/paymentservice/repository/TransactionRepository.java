package com.omnicharge.paymentservice.repository;

import com.omnicharge.paymentservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByRechargeId(Long rechargeId);
}
