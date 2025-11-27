package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
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
        // durable = true
        return new Queue(DEVICE_DATA_QUEUE, true);
    }

    @Bean
    public Binding deviceDataBinding(Queue deviceDataQueue, DirectExchange deviceDataExchange) {
        return BindingBuilder
                .bind(deviceDataQueue)
                .to(deviceDataExchange)
                .with(DEVICE_DATA_ROUTING_KEY);
    }

    // ⚠️ Asta e NOUL factory, pe care îl vom folosi explicit în @RabbitListener
    @Bean
    public SimpleRabbitListenerContainerFactory myRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // Primește payload-ul ca String, fără header-mapping fancy
        factory.setMessageConverter(new SimpleMessageConverter());

        return factory;
    }
}
