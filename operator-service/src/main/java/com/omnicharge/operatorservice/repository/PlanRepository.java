package com.omnicharge.operatorservice.repository;

import com.omnicharge.operatorservice.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByOperatorId(Long operatorId);
}
