package com.example.demo.realtimesupportservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String OVERCONSUMPTION_ALERTS_QUEUE = "overconsumption.alerts";

    @Bean
    public Queue overconsumptionAlertsQueue() {
        // durable=true ca să rămână după restart
        return new Queue(OVERCONSUMPTION_ALERTS_QUEUE, true);
    }
}
