package com.omnicharge.rechargeservice.service;

import com.omnicharge.rechargeservice.client.OperatorClient;
import com.omnicharge.rechargeservice.dto.PlanDto;
import com.omnicharge.rechargeservice.dto.RechargeEvent;
import com.omnicharge.rechargeservice.dto.RechargeRequest;
import com.omnicharge.rechargeservice.model.RechargeRecord;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RechargeService {

    @Autowired
    private RechargeRepository rechargeRepository;

    @Autowired
    private OperatorClient operatorClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.payment}")
    private String paymentRoutingKey;

    public RechargeRecord initiateRecharge(Long userId, RechargeRequest request) {
        PlanDto plan = operatorClient.getPlanById(request.getPlanId());
        if (plan == null) {
            throw new RuntimeException("Plan not found!");
        }

        RechargeRecord record = new RechargeRecord(
                userId,
                request.getMobileNumber(),
                request.getOperatorId(),
                request.getPlanId(),
                plan.getPrice()
        );

        record = rechargeRepository.save(record);

        RechargeEvent event = new RechargeEvent(
                record.getId(),
                record.getUserId(),
                record.getMobileNumber(),
                record.getAmount(),
                record.getStatus()
        );
        
        rabbitTemplate.convertAndSend(exchange, paymentRoutingKey, event);

        return record;
    }
}
