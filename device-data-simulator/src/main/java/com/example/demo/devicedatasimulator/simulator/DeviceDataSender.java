package com.example.demo.devicedatasimulator.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
public class DeviceDataSender {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDataSender.class);

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    private static final String EXCHANGE_NAME = "device-data-exchange";
    private static final String ROUTING_KEY = "device.data";

    private final UUID deviceId;

    // Intervalul îl luăm din application.properties
    private final long intervalMs;

    public DeviceDataSender(RabbitTemplate rabbitTemplate,
                            @Value("${simulator.device-id}") String deviceIdString,
                            @Value("${simulator.interval-ms}") long intervalMs) {
        this.rabbitTemplate = rabbitTemplate;
        this.intervalMs = intervalMs;
        this.deviceId = UUID.fromString(deviceIdString);
        logger.info("DeviceDataSender initialized for device {}", this.deviceId);
    }

    // trimitem mesaj la fiecare interval (default 10 sec)
    @Scheduled(fixedRateString = "${simulator.interval-ms}")
    public void sendMeasurement() {
        String timestamp = Instant.now().toString();

        // Valoare random între 0.05 și 0.40 kWh
        double min = 0.05;
        double max = 0.40;
        double value = min + (max - min) * random.nextDouble();

        String payload = String.format(
                "{\"timestamp\":\"%s\",\"deviceId\":\"%s\",\"measurementValue\":%.4f}",
                timestamp, deviceId, value
        );

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, payload);

        logger.info("Sent measurement: {}", payload);
    }
}
