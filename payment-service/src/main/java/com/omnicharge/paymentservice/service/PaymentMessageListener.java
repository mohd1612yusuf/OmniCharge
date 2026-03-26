package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.RechargeEvent;
import com.omnicharge.paymentservice.model.Transaction;
import com.omnicharge.paymentservice.repository.TransactionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentMessageListener {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.notification-routing}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.recharge-update-routing}")
    private String rechargeUpdateRoutingKey;

    @RabbitListener(queues = "${rabbitmq.payment-queue}")
    public void processPayment(RechargeEvent event) {
        String refNumber = UUID.randomUUID().toString();

        // Save the transaction; transactionDate is set to LocalDateTime.now() inside the constructor
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getUserId(),
                event.getAmount(),
                "SUCCESS",
                refNumber
        );

        Transaction saved = transactionRepository.save(transaction);

        // Update the event with SUCCESS and the exact transaction date
        event.setStatus("SUCCESS");
        event.setTransactionDate(saved.getTransactionDate());

        System.out.println("Payment processed for recharge ID: " + event.getTransactionId()
                + " | Ref: " + refNumber + " | Date: " + saved.getTransactionDate());

        // Notify notification-service (SMS/email simulation)
        rabbitTemplate.convertAndSend(exchange, notificationRoutingKey, event);

        // Notify recharge-service to update recharge status and completion_time
        rabbitTemplate.convertAndSend(exchange, rechargeUpdateRoutingKey, event);
    }
}
