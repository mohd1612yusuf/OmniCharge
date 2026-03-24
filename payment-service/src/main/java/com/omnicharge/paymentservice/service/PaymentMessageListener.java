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

    @RabbitListener(queues = "${rabbitmq.payment-queue}")
    public void processPayment(RechargeEvent event) {
        String refNumber = UUID.randomUUID().toString();
        
        Transaction transaction = new Transaction(
                event.getTransactionId(),
                event.getUserId(),
                event.getAmount(),
                "SUCCESS",
                refNumber
        );

        transactionRepository.save(transaction);

        event.setStatus("SUCCESS");
        
        System.out.println("Payment processed successfully for recharge ID: " + event.getTransactionId());

        rabbitTemplate.convertAndSend(exchange, notificationRoutingKey, event);
    }
}
