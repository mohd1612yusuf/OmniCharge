package com.omnicharge.operatorservice.controller;

import com.omnicharge.operatorservice.model.Operator;
import com.omnicharge.operatorservice.model.Plan;
import com.omnicharge.operatorservice.repository.OperatorRepository;
import com.omnicharge.operatorservice.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/operators")
public class OperatorController {

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private PlanRepository planRepository;

    @GetMapping
    public ResponseEntity<List<Operator>> getAllOperators() {
        return ResponseEntity.ok(operatorRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Operator> createOperator(@RequestBody Operator operator) {
        return ResponseEntity.ok(operatorRepository.save(operator));
    }

    @GetMapping("/{operatorId}/plans")
    public ResponseEntity<List<Plan>> getPlansByOperator(@PathVariable("operatorId") Long operatorId) {
        return ResponseEntity.ok(planRepository.findByOperatorId(operatorId));
    }

    @PostMapping("/{operatorId}/plans")
    public ResponseEntity<?> createPlan(@PathVariable("operatorId") Long operatorId, @RequestBody Plan plan) {
        Optional<Operator> operatorOpt = operatorRepository.findById(operatorId);
        if (operatorOpt.isPresent()) {
            plan.setOperator(operatorOpt.get());
            return ResponseEntity.ok(planRepository.save(plan));
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/plans/{planId}")
    public ResponseEntity<Plan> getPlanById(@PathVariable("planId") Long planId) {
        Optional<Plan> plan = planRepository.findById(planId);
        return plan.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
