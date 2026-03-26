package com.omnicharge.operatorservice.controller;

import com.omnicharge.operatorservice.model.Operator;
import com.omnicharge.operatorservice.model.Plan;
import com.omnicharge.operatorservice.repository.OperatorRepository;
import com.omnicharge.operatorservice.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperatorControllerTest {

    @Mock
    private OperatorRepository operatorRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private OperatorController operatorController;

    // ─── getAllOperators ────────────────────────────────────────────────────────

    @Test
    public void getAllOperators_returnsListOfOperators() {
        Operator o1 = new Operator(); o1.setId(1L); o1.setName("Airtel");
        Operator o2 = new Operator(); o2.setId(2L); o2.setName("Jio");
        when(operatorRepository.findAll()).thenReturn(Arrays.asList(o1, o2));

        ResponseEntity<List<Operator>> response = operatorController.getAllOperators();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(operatorRepository, times(1)).findAll();
    }

    @Test
    public void getAllOperators_emptyList_returnsOk() {
        when(operatorRepository.findAll()).thenReturn(List.of());
        ResponseEntity<List<Operator>> response = operatorController.getAllOperators();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ─── createOperator ────────────────────────────────────────────────────────

    @Test
    public void createOperator_savesAndReturnsOperator() {
        Operator toSave = new Operator(); toSave.setName("Vi");
        Operator saved  = new Operator(); saved.setId(3L); saved.setName("Vi");
        when(operatorRepository.save(toSave)).thenReturn(saved);

        ResponseEntity<Operator> response = operatorController.createOperator(toSave);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3L, response.getBody().getId());
        verify(operatorRepository, times(1)).save(toSave);
    }

    // ─── getPlansByOperator ────────────────────────────────────────────────────

    @Test
    public void getPlansByOperator_returnsPlanList() {
        Plan p1 = new Plan(); p1.setId(10L); p1.setName("Basic");
        when(planRepository.findByOperatorId(1L)).thenReturn(List.of(p1));

        ResponseEntity<List<Plan>> response = operatorController.getPlansByOperator(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(planRepository, times(1)).findByOperatorId(1L);
    }

    // ─── createPlan ───────────────────────────────────────────────────────────

    @Test
    public void createPlan_operatorExists_returnsSavedPlan() {
        Operator op = new Operator(); op.setId(1L); op.setName("Airtel");
        Plan plan = new Plan(); plan.setName("5G Unlimited"); plan.setPrice(new BigDecimal("299.00")); plan.setValidityDays(28);
        Plan saved = new Plan(); saved.setId(20L); saved.setName("5G Unlimited");

        when(operatorRepository.findById(1L)).thenReturn(Optional.of(op));
        when(planRepository.save(any(Plan.class))).thenReturn(saved);

        ResponseEntity<?> response = operatorController.createPlan(1L, plan);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    public void createPlan_operatorNotFound_returns404() {
        when(operatorRepository.findById(99L)).thenReturn(Optional.empty());
        Plan plan = new Plan(); plan.setName("Plan");

        ResponseEntity<?> response = operatorController.createPlan(99L, plan);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(planRepository, never()).save(any());
    }

    // ─── getPlanById ──────────────────────────────────────────────────────────

    @Test
    public void getPlanById_planExists_returnsPlan() {
        Plan p = new Plan(); p.setId(5L); p.setName("Data Pack");
        when(planRepository.findById(5L)).thenReturn(Optional.of(p));

        ResponseEntity<Plan> response = operatorController.getPlanById(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().getId());
    }

    @Test
    public void getPlanById_planNotFound_returns404() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Plan> response = operatorController.getPlanById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
