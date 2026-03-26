package com.omnicharge.rechargeservice.listener;

import com.omnicharge.rechargeservice.dto.RechargeEvent;
import com.omnicharge.rechargeservice.model.RechargeRecord;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RechargeUpdateListenerTest {

    @Mock
    private RechargeRepository rechargeRepository;

    @InjectMocks
    private RechargeUpdateListener rechargeUpdateListener;

    @Test
    public void onPaymentCompleted_recordFound_updatesStatusAndCompletionTime() {
        LocalDateTime txnDate = LocalDateTime.of(2026, 3, 26, 12, 30, 0);

        RechargeEvent event = new RechargeEvent();
        event.setTransactionId(1L);
        event.setStatus("SUCCESS");
        event.setTransactionDate(txnDate);

        RechargeRecord record = new RechargeRecord(1L, "9876543210", 1L, 1L, new BigDecimal("99.00"));
        record.setId(1L);
        record.setStatus("PENDING");

        when(rechargeRepository.findById(1L)).thenReturn(Optional.of(record));
        when(rechargeRepository.save(any(RechargeRecord.class))).thenReturn(record);

        rechargeUpdateListener.onPaymentCompleted(event);

        // Capture what was saved
        ArgumentCaptor<RechargeRecord> captor = ArgumentCaptor.forClass(RechargeRecord.class);
        verify(rechargeRepository).save(captor.capture());

        RechargeRecord saved = captor.getValue();
        assertEquals("SUCCESS", saved.getStatus());
        assertEquals(txnDate, saved.getCompletionTime());
    }

    @Test
    public void onPaymentCompleted_nullTransactionDate_usesCurrentTime() {
        RechargeEvent event = new RechargeEvent();
        event.setTransactionId(2L);
        event.setStatus("SUCCESS");
        event.setTransactionDate(null); // null date case

        RechargeRecord record = new RechargeRecord(2L, "8888888888", 1L, 2L, new BigDecimal("49.00"));
        record.setId(2L);

        when(rechargeRepository.findById(2L)).thenReturn(Optional.of(record));
        when(rechargeRepository.save(any(RechargeRecord.class))).thenReturn(record);

        rechargeUpdateListener.onPaymentCompleted(event);

        ArgumentCaptor<RechargeRecord> captor = ArgumentCaptor.forClass(RechargeRecord.class);
        verify(rechargeRepository).save(captor.capture());

        // Completion time should be set (fallback to now)
        assertNotNull(captor.getValue().getCompletionTime());
    }

    @Test
    public void onPaymentCompleted_recordNotFound_doesNotSave() {
        RechargeEvent event = new RechargeEvent();
        event.setTransactionId(99L);
        event.setStatus("SUCCESS");
        event.setTransactionDate(LocalDateTime.now());

        when(rechargeRepository.findById(99L)).thenReturn(Optional.empty());

        rechargeUpdateListener.onPaymentCompleted(event);

        // Should never save if record not found
        verify(rechargeRepository, never()).save(any());
    }
}
