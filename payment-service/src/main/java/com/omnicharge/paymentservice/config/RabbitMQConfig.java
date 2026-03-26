package com.omnicharge.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.payment-queue}")
    private String paymentQueue;

    @Value("${rabbitmq.payment-routing}")
    private String paymentRoutingKey;

    @Value("${rabbitmq.notification-queue}")
    private String notificationQueue;
    
    @Value("${rabbitmq.notification-routing}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.recharge-update-queue}")
    private String rechargeUpdateQueue;

    @Value("${rabbitmq.recharge-update-routing}")
    private String rechargeUpdateRoutingKey;

    @Bean
    public Queue paymentQueue() {
        return new Queue(paymentQueue);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue);
    }

    @Bean
    public Queue rechargeUpdateQueue() {
        return new Queue(rechargeUpdateQueue);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding paymentBinding(@Qualifier("paymentQueue") Queue paymentQueue, DirectExchange exchange) {
        return BindingBuilder.bind(paymentQueue).to(exchange).with(paymentRoutingKey);
    }

    @Bean
    public Binding notificationBinding(@Qualifier("notificationQueue") Queue notificationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with(notificationRoutingKey);
    }

    @Bean
    public Binding rechargeUpdateBinding(@Qualifier("rechargeUpdateQueue") Queue rechargeUpdateQueue, DirectExchange exchange) {
        return BindingBuilder.bind(rechargeUpdateQueue).to(exchange).with(rechargeUpdateRoutingKey);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
