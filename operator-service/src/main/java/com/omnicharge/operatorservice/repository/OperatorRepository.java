package com.omnicharge.operatorservice.repository;

import com.omnicharge.operatorservice.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByName(String name);
}
