package com.omnicharge.rechargeservice.service;

import com.omnicharge.rechargeservice.client.OperatorClient;
import com.omnicharge.rechargeservice.dto.PlanDto;
import com.omnicharge.rechargeservice.dto.RechargeRequest;
import com.omnicharge.rechargeservice.model.RechargeRecord;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RechargeServiceTest {

    @Mock
    private RechargeRepository rechargeRepository;

    @Mock
    private OperatorClient operatorClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RechargeService rechargeService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(rechargeService, "exchange", "test_exchange");
        ReflectionTestUtils.setField(rechargeService, "paymentRoutingKey", "test_routing");
    }

    // ─── Happy path ──────────────────────────────────────────────────────────

    @Test
    public void initiateRecharge_validRequest_savesAndPublishes() {
        Long userId = 1L;
        RechargeRequest request = new RechargeRequest();
        request.setMobileNumber("9876543210");
        request.setOperatorId(2L);
        request.setPlanId(3L);

        PlanDto mockPlan = new PlanDto();
        mockPlan.setId(3L);
        mockPlan.setPrice(new BigDecimal("99.99"));

        RechargeRecord savedRecord = new RechargeRecord(userId, "9876543210", 2L, 3L, new BigDecimal("99.99"));
        savedRecord.setId(100L);

        when(operatorClient.getPlanById(3L)).thenReturn(mockPlan);
        when(rechargeRepository.save(any(RechargeRecord.class))).thenReturn(savedRecord);

        RechargeRecord result = rechargeService.initiateRecharge(userId, request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(new BigDecimal("99.99"), result.getAmount());
        assertEquals("9876543210", result.getMobileNumber());

        verify(operatorClient, times(1)).getPlanById(3L);
        verify(rechargeRepository, times(1)).save(any(RechargeRecord.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("test_exchange"), eq("test_routing"), (Object) any());
    }

    @Test
    public void initiateRecharge_amountFromPlanIsCorrect() {
        Long userId = 5L;
        RechargeRequest request = new RechargeRequest();
        request.setMobileNumber("8000000000");
        request.setOperatorId(1L);
        request.setPlanId(10L);

        PlanDto plan = new PlanDto();
        plan.setId(10L);
        plan.setPrice(new BigDecimal("199.00"));

        RechargeRecord saved = new RechargeRecord(5L, "8000000000", 1L, 10L, new BigDecimal("199.00"));
        saved.setId(200L);

        when(operatorClient.getPlanById(10L)).thenReturn(plan);
        when(rechargeRepository.save(any(RechargeRecord.class))).thenReturn(saved);

        RechargeRecord result = rechargeService.initiateRecharge(userId, request);

        assertEquals(new BigDecimal("199.00"), result.getAmount());
    }

    // ─── Error path ───────────────────────────────────────────────────────────

    @Test
    public void initiateRecharge_planNotFound_throwsRuntimeException() {
        Long userId = 1L;
        RechargeRequest request = new RechargeRequest();
        request.setMobileNumber("9999999999");
        request.setPlanId(99L);
        request.setOperatorId(1L);

        when(operatorClient.getPlanById(99L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                rechargeService.initiateRecharge(userId, request)
        );

        assertEquals("Plan not found!", ex.getMessage());

        // Ensure nothing was saved or published
        verify(rechargeRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    public void initiateRecharge_userIdIsStoredInRecord() {
        Long userId = 42L;
        RechargeRequest request = new RechargeRequest();
        request.setMobileNumber("7777777777");
        request.setOperatorId(3L);
        request.setPlanId(5L);

        PlanDto plan = new PlanDto();
        plan.setId(5L);
        plan.setPrice(new BigDecimal("49.00"));

        RechargeRecord saved = new RechargeRecord(42L, "7777777777", 3L, 5L, new BigDecimal("49.00"));
        saved.setId(300L);

        when(operatorClient.getPlanById(5L)).thenReturn(plan);
        when(rechargeRepository.save(any(RechargeRecord.class))).thenReturn(saved);

        RechargeRecord result = rechargeService.initiateRecharge(userId, request);

        assertEquals(42L, result.getUserId());
    }
}
