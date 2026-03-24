package com.omnicharge.rechargeservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.omnicharge.rechargeservice.client.OperatorClient;
import com.omnicharge.rechargeservice.dto.PlanDto;
import com.omnicharge.rechargeservice.dto.RechargeRequest;
import com.omnicharge.rechargeservice.model.RechargeRecord;
import com.omnicharge.rechargeservice.repository.RechargeRepository;

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

	@Test
	public void testInitiateRechargeSuccess() {
		Long userId = 1L;
		RechargeRequest request = new RechargeRequest();
		request.setMobileNumber("1234567890");
		request.setOperatorId(2L);
		request.setPlanId(3L);

		PlanDto mockPlan = new PlanDto();
		mockPlan.setId(3L);
		mockPlan.setPrice(new BigDecimal("99.99"));

		RechargeRecord mockRecord = new RechargeRecord(userId, "1234567890", 2L, 3L, new BigDecimal("99.99"));
		mockRecord.setId(100L);

		when(operatorClient.getPlanById(3L)).thenReturn(mockPlan);
		when(rechargeRepository.save(any(RechargeRecord.class))).thenReturn(mockRecord);

		RechargeRecord result = rechargeService.initiateRecharge(userId, request);

		assertNotNull(result);
		assertEquals(100L, result.getId());
		assertEquals("PENDING", result.getStatus());
		assertEquals(new BigDecimal("99.99"), result.getAmount());

		verify(operatorClient, times(1)).getPlanById(3L);
		verify(rechargeRepository, times(1)).save(any(RechargeRecord.class));
		verify(rabbitTemplate, times(1)).convertAndSend(eq("test_exchange"), eq("test_routing"), (Object) any());
	}

//    @Test
//    public void testInitiateRechargePlanNotFound() {
//        Long userId = 1L;
//        RechargeRequest request = new RechargeRequest();
//        request.setPlanId(99L);
//
//        when(operatorClient.getPlanById(99L)).thenReturn(null);
//
//        assertThrows(RuntimeException.class, () -> {
//            rechargeService.initiateRecharge(userId, request);
//        });
//
//        verify(rechargeRepository, never()).save(any());
//        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
//    }
}
