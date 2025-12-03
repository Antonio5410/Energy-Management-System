package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange syncExchange() {
        return new DirectExchange("sync.exchange");
    }

    @Bean
    public Queue syncQueue() {
        return new Queue("sync.queue", true);
    }

    @Bean
    public Binding syncBinding(DirectExchange syncExchange, Queue syncQueue) {
        return BindingBuilder.bind(syncQueue).to(syncExchange).with("sync.key");
    }
}
