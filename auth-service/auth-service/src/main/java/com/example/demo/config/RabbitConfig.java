package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${sync.exchange}")
    private String exchangeName;

    @Value("${sync.routing-key}")
    private String routingKey;

    @Value("${sync.queue}")
    private String queueName;

    @Bean
    public DirectExchange syncExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue syncQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding syncBinding(Queue syncQueue, DirectExchange syncExchange) {
        return BindingBuilder.bind(syncQueue).to(syncExchange).with(routingKey);
    }
}
