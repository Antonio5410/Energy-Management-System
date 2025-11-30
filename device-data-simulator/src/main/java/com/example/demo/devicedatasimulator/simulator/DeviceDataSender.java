package com.example.demo.devicedatasimulator.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

    public DeviceDataSender(
            RabbitTemplate rabbitTemplate,
            @Value("${simulator.device-id}") String deviceIdString
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.deviceId = UUID.fromString(deviceIdString);
        logger.info("DeviceDataSender initialized for device {}", this.deviceId);
    }

    // trimitem mesaj la fiecare 10 secunde (sau ce ai în simulator.interval-ms)
    @Scheduled(fixedRateString = "${simulator.interval-ms}")
    public void sendMeasurement() {
        String timestamp = Instant.now().toString();
//        OffsetDateTime timestamp = OffsetDateTime
//                .now(ZoneOffset.UTC)
//                .withSecond(0)
//                .withNano(0);



        // valoare random între 0.05 și 0.40
        double min = 0.05;
        double max = 0.40;
        double value = min + (max - min) * random.nextDouble();

        // BigDecimal NU depinde de locale: toPlainString are întotdeauna punct, nu virgulă
        BigDecimal bd = BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
        String numericValue = bd.toPlainString(); // ex: "0.2345"

        String payload = "{\"timestamp\":\"" + timestamp + "\"," +
                "\"deviceId\":\"" + deviceId + "\"," +
                "\"measurementValue\":" + numericValue +
                "}";

        logger.info("Sending payload: {}", payload);

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, payload);
    }
}
