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

    // ─────────────────────────────────────────────────────────────────────────
    // OPERATOR ENDPOINTS  (all protected by ROLE_ADMIN via API Gateway)
    // ─────────────────────────────────────────────────────────────────────────

    /** GET /api/operators — list all operators (ROLE_ADMIN) */
    @GetMapping
    public ResponseEntity<List<Operator>> getAllOperators() {
        return ResponseEntity.ok(operatorRepository.findAll());
    }

    /** POST /api/operators — create a new operator (ROLE_ADMIN) */
    @PostMapping
    public ResponseEntity<Operator> createOperator(@RequestBody Operator operator) {
        return ResponseEntity.ok(operatorRepository.save(operator));
    }

    /** PUT /api/operators/{operatorId} — update operator name (ROLE_ADMIN) */
    @PutMapping("/{operatorId}")
    public ResponseEntity<?> updateOperator(@PathVariable("operatorId") Long operatorId,
                                            @RequestBody Operator updatedOperator) {
        Optional<Operator> existing = operatorRepository.findById(operatorId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Operator operator = existing.get();
        operator.setName(updatedOperator.getName());
        Operator saved = operatorRepository.save(operator);
        return ResponseEntity.ok(saved);
    }

    /** DELETE /api/operators/{operatorId} — delete an operator and its plans (ROLE_ADMIN) */
    @DeleteMapping("/{operatorId}")
    public ResponseEntity<?> deleteOperator(@PathVariable("operatorId") Long operatorId) {
        if (!operatorRepository.existsById(operatorId)) {
            return ResponseEntity.notFound().build();
        }
        // Delete associated plans first to avoid FK constraint violation
        List<Plan> plans = planRepository.findByOperatorId(operatorId);
        planRepository.deleteAll(plans);

        operatorRepository.deleteById(operatorId);
        return ResponseEntity.ok("Operator with ID " + operatorId + " deleted successfully.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLAN ENDPOINTS  (all protected by ROLE_ADMIN via API Gateway)
    // ─────────────────────────────────────────────────────────────────────────

    /** GET /api/operators/{operatorId}/plans — list all plans of an operator (ROLE_ADMIN) */
    @GetMapping("/{operatorId}/plans")
    public ResponseEntity<List<Plan>> getPlansByOperator(@PathVariable("operatorId") Long operatorId) {
        return ResponseEntity.ok(planRepository.findByOperatorId(operatorId));
    }

    /** POST /api/operators/{operatorId}/plans — create a new plan (ROLE_ADMIN) */
    @PostMapping("/{operatorId}/plans")
    public ResponseEntity<?> createPlan(@PathVariable("operatorId") Long operatorId,
                                        @RequestBody Plan plan) {
        Optional<Operator> operatorOpt = operatorRepository.findById(operatorId);
        if (operatorOpt.isPresent()) {
            plan.setOperator(operatorOpt.get());
            return ResponseEntity.ok(planRepository.save(plan));
        }
        return ResponseEntity.notFound().build();
    }

    /** GET /api/operators/plans/{planId} — get a specific plan by ID (ROLE_ADMIN) */
    @GetMapping("/plans/{planId}")
    public ResponseEntity<Plan> getPlanById(@PathVariable("planId") Long planId) {
        Optional<Plan> plan = planRepository.findById(planId);
        return plan.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** PUT /api/operators/plans/{planId} — update a plan's details (ROLE_ADMIN) */
    @PutMapping("/plans/{planId}")
    public ResponseEntity<?> updatePlan(@PathVariable("planId") Long planId,
                                        @RequestBody Plan updatedPlan) {
        Optional<Plan> existing = planRepository.findById(planId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Plan plan = existing.get();

        // Update only modifiable fields; operator association stays the same
        if (updatedPlan.getName() != null)        plan.setName(updatedPlan.getName());
        if (updatedPlan.getPrice() != null)       plan.setPrice(updatedPlan.getPrice());
        if (updatedPlan.getValidityDays() != null) plan.setValidityDays(updatedPlan.getValidityDays());
        if (updatedPlan.getDescription() != null) plan.setDescription(updatedPlan.getDescription());

        return ResponseEntity.ok(planRepository.save(plan));
    }

    /** DELETE /api/operators/plans/{planId} — delete a plan by ID (ROLE_ADMIN) */
    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable("planId") Long planId) {
        if (!planRepository.existsById(planId)) {
            return ResponseEntity.notFound().build();
        }
        planRepository.deleteById(planId);
        return ResponseEntity.ok("Plan with ID " + planId + " deleted successfully.");
    }
}
