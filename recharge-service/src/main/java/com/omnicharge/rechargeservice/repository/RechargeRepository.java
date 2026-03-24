package com.omnicharge.rechargeservice.repository;

import com.omnicharge.rechargeservice.model.RechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RechargeRepository extends JpaRepository<RechargeRecord, Long> {
    List<RechargeRecord> findByUserId(Long userId);
}
