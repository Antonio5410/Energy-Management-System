package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.example.demo.dtos.OverconsumptionAlertDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OverconsumptionAlertPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OverconsumptionAlertPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OverconsumptionAlertDTO alert) {
        rabbitTemplate.convertAndSend(RabbitConfig.OVERCONSUMPTION_QUEUE, alert);
    }
}
