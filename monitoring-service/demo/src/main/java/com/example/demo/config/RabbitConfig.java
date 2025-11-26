package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String DEVICE_DATA_EXCHANGE = "device-data-exchange";
    public static final String DEVICE_DATA_QUEUE = "device-data-queue";
    public static final String DEVICE_DATA_ROUTING_KEY = "device.data";

    @Bean
    public DirectExchange deviceDataExchange() {
        return new DirectExchange(DEVICE_DATA_EXCHANGE);
    }

    @Bean
    public Queue deviceDataQueue() {
        // durable = true, autoDelete = false
        return new Queue(DEVICE_DATA_QUEUE, true);
    }

    @Bean
    public Binding deviceDataBinding(Queue deviceDataQueue, DirectExchange deviceDataExchange) {
        return BindingBuilder
                .bind(deviceDataQueue)
                .to(deviceDataExchange)
                .with(DEVICE_DATA_ROUTING_KEY);
    }
}
