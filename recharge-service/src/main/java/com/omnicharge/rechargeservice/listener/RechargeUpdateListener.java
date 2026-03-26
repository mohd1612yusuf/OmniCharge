package com.omnicharge.rechargeservice.listener;

import com.omnicharge.rechargeservice.dto.RechargeEvent;
import com.omnicharge.rechargeservice.model.RechargeRecord;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RechargeUpdateListener {

    @Autowired
    private RechargeRepository rechargeRepository;

    @RabbitListener(queues = "${rabbitmq.recharge-update-queue}")
    public void onPaymentCompleted(RechargeEvent event) {
        Long rechargeId = event.getTransactionId();

        Optional<RechargeRecord> optional = rechargeRepository.findById(rechargeId);
        if (optional.isPresent()) {
            RechargeRecord record = optional.get();

            // Update status to SUCCESS
            record.setStatus("SUCCESS");

            // Set completion_time to the exact transaction_date from payment-service
            if (event.getTransactionDate() != null) {
                record.setCompletionTime(event.getTransactionDate());
            } else {
                // Fallback: use current time in case transactionDate was null
                record.setCompletionTime(java.time.LocalDateTime.now());
            }

            rechargeRepository.save(record);

            System.out.println("[RechargeUpdateListener] Recharge ID " + rechargeId
                    + " updated to SUCCESS at " + record.getCompletionTime());
        } else {
            System.err.println("[RechargeUpdateListener] WARNING: Recharge ID " + rechargeId + " not found!");
        }
    }
}
