package com.example.demo.services; // sau .service, cum ai tu pachetul

import com.example.demo.config.RabbitConfig;
import com.example.demo.dtos.DeviceMeasurementMessage;
import com.example.demo.entities.HourlyConsumption;
import com.example.demo.repositories.HourlyConsumptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceMeasurementListener {

    private static final Logger logger = LoggerFactory.getLogger(DeviceMeasurementListener.class);

    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceMeasurementListener(HourlyConsumptionRepository hourlyConsumptionRepository) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
    }

    @Transactional
    @RabbitListener(
            queues = RabbitConfig.DEVICE_DATA_QUEUE,
            containerFactory = "myRabbitListenerContainerFactory"
    )
    public void handleDeviceMeasurement(String payload) {
        logger.info("Received raw message payload: {}", payload);

        try {
            DeviceMeasurementMessage message =
                    objectMapper.readValue(payload, DeviceMeasurementMessage.class);

            logger.info("Parsed message: {}", message);

            // 2. Parse deviceId
            UUID deviceId = UUID.fromString(message.getDeviceId());

            // 3. Parse timestamp (ISO-8601, ex: 2025-11-26T15:40:00Z)
            OffsetDateTime odt = OffsetDateTime.parse(message.getTimestamp());
            LocalDateTime localTimestamp = odt.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

            // 4. Trunchiem la începutul orei
            LocalDateTime hourStart = localTimestamp
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            Double value = message.getMeasurementValue();
            if (value == null) {
                logger.warn("Measurement value is null, ignoring message");
                return;
            }

            // 5. Căutăm dacă există deja consum pentru device + ora respectivă
            Optional<HourlyConsumption> existingOpt =
                    hourlyConsumptionRepository.findByDeviceIdAndHourStart(deviceId, hourStart);

            if (existingOpt.isPresent()) {
                HourlyConsumption existing = existingOpt.get();
                existing.setEnergyKwh(existing.getEnergyKwh() + value);
                hourlyConsumptionRepository.save(existing);
                logger.info("Updated hourly consumption: deviceId={}, hourStart={}, newEnergy={}",
                        deviceId, hourStart, existing.getEnergyKwh());
            } else {
                HourlyConsumption newEntry = new HourlyConsumption(
                        deviceId,
                        hourStart,
                        value
                );
                hourlyConsumptionRepository.save(newEntry);
                logger.info("Created new hourly consumption: deviceId={}, hourStart={}, energy={}",
                        deviceId, hourStart, value);
            }

        } catch (Exception e) {
            logger.error("Error while processing measurement message", e);
        }
    }
}
