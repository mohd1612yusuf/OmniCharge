package com.omnicharge.notificationservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.omnicharge.notificationservice.dto.RechargeEvent;

@Service
public class NotificationListener {

    @RabbitListener(queues = "${rabbitmq.notification-queue}")
    public void processNotification(RechargeEvent event) {
        System.out.println("=================================================");
        System.out.println("NOTIFICATION SENT TO MOBILE: " + event.getMobileNumber());
        System.out.println("Dear Customer, your recharge for Transaction ID: " + event.getTransactionId() + 
                           " with amount " + event.getAmount() + " is " + event.getStatus());
        System.out.println("=================================================");
    }
}
