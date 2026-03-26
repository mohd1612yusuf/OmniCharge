package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.RechargeEvent;
import com.omnicharge.paymentservice.model.Transaction;
import com.omnicharge.paymentservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentMessageListenerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentMessageListener paymentMessageListener;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(paymentMessageListener, "exchange", "payment_exchange");
        ReflectionTestUtils.setField(paymentMessageListener, "notificationRoutingKey", "notification_routing_key");
        ReflectionTestUtils.setField(paymentMessageListener, "rechargeUpdateRoutingKey", "recharge_update_routing_key");
    }

    @Test
    public void processPayment_saveTransactionAndPublishesToTwoQueues() {
        RechargeEvent event = new RechargeEvent(1L, 100L, "9876543210", new BigDecimal("99.00"), "PENDING");

        Transaction savedTxn = new Transaction(1L, 100L, new BigDecimal("99.00"), "SUCCESS", "REF-001");
        // Set a specific date to verify it's forwarded
        LocalDateTime txnDate = LocalDateTime.of(2026, 3, 26, 10, 0, 0);
        savedTxn.setTransactionDate(txnDate);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTxn);

        paymentMessageListener.processPayment(event);

        // Verify transaction was saved
        verify(transactionRepository, times(1)).save(any(Transaction.class));

        // Verify published to notification queue
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("payment_exchange"), eq("notification_routing_key"), any(RechargeEvent.class)
        );

        // Verify published to recharge update queue
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("payment_exchange"), eq("recharge_update_routing_key"), any(RechargeEvent.class)
        );

        // Verify event status was updated to SUCCESS
        assertEquals("SUCCESS", event.getStatus());
    }

    @Test
    public void processPayment_eventStatusSetToSuccess() {
        RechargeEvent event = new RechargeEvent(2L, 200L, "9000000000", new BigDecimal("199.00"), "PENDING");

        Transaction saved = new Transaction(2L, 200L, new BigDecimal("199.00"), "SUCCESS", "REF-002");
        saved.setTransactionDate(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        paymentMessageListener.processPayment(event);

        assertEquals("SUCCESS", event.getStatus());
        assertNotNull(event.getTransactionDate());
    }

    @Test
    public void processPayment_capturedTransactionHasCorrectFields() {
        RechargeEvent event = new RechargeEvent(3L, 300L, "8000000000", new BigDecimal("49.00"), "PENDING");
        Transaction saved = new Transaction(3L, 300L, new BigDecimal("49.00"), "SUCCESS", "REF-003");
        saved.setTransactionDate(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        paymentMessageListener.processPayment(event);

        // Capture the saved transaction to inspect
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction captured = captor.getValue();
        assertEquals(3L, captured.getRechargeId());
        assertEquals(300L, captured.getUserId());
        assertEquals("SUCCESS", captured.getPaymentStatus());
        assertEquals(new BigDecimal("49.00"), captured.getAmount());
        assertNotNull(captured.getReferenceNumber(), "Reference number should be generated");
    }
}
