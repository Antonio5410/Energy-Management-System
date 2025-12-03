package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String SYNC_EXCHANGE = "sync.exchange";
    public static final String SYNC_QUEUE = "sync.queue";
    public static final String SYNC_ROUTING_KEY = "sync.key";

    @Bean
    public DirectExchange syncExchange() {
        return new DirectExchange(SYNC_EXCHANGE);
    }

    @Bean
    public Queue syncQueue() {
        // durable = true (să rămână la restart)
        return new Queue(SYNC_QUEUE, true);
    }

    @Bean
    public Binding syncBinding(DirectExchange syncExchange, Queue syncQueue) {
        return BindingBuilder
                .bind(syncQueue)
                .to(syncExchange)
                .with(SYNC_ROUTING_KEY);
    }
}
