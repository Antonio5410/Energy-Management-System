package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;



@Configuration
public class RabbitConfig {

    public static final String DEVICE_DATA_EXCHANGE = "device-data-exchange";
    public static final String DEVICE_DATA_QUEUE = "device-data-queue";
    public static final String DEVICE_DATA_ROUTING_KEY = "device.data";
    public static final String OVERCONSUMPTION_QUEUE = "overconsumption.alerts";

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

    @Bean
    public Queue overconsumptionQueue() {
        return new Queue(OVERCONSUMPTION_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }



    @Bean
    public SimpleRabbitListenerContainerFactory myRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        factory.setMessageConverter(new RawJsonMessageConverter());

        return factory;
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    /// pentru device sync cu device service

    public static final String SYNC_EXCHANGE = "sync.exchange";
    public static final String DEVICE_SYNC_QUEUE = "device-sync.queue";
    public static final String DEVICE_SYNC_ROUTING_KEY = "device-sync.key";

    @Bean
    public DirectExchange syncExchange() {
        return new DirectExchange(SYNC_EXCHANGE);
    }

    @Bean
    public Queue deviceSyncQueue() {
        // coadă separată pentru DEVICE_CREATED
        return new Queue(DEVICE_SYNC_QUEUE, true);
    }

    @Bean
    public Binding deviceSyncBinding(DirectExchange syncExchange, Queue deviceSyncQueue) {
        return BindingBuilder
                .bind(deviceSyncQueue)
                .to(syncExchange)
                .with(DEVICE_SYNC_ROUTING_KEY);
    }
    @Bean
    public SimpleRabbitListenerContainerFactory deviceSyncListenerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.addAllowedListPatterns("com.example.demo.dtos.*");
        converter.addAllowedListPatterns("java.util.*");
        converter.addAllowedListPatterns("java.time.*");

        factory.setMessageConverter(converter);
        return factory;
    }

}
