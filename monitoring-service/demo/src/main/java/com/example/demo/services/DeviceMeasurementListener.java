package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.example.demo.dtos.DeviceMeasurementMessage;
import com.example.demo.dtos.OverconsumptionAlertDTO;
import com.example.demo.entities.HourlyConsumption;
import com.example.demo.repositories.HourlyConsumptionRepository;
import com.example.demo.repositories.MonitoredDeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import com.example.demo.entities.MonitoredDevice;

@Service
public class DeviceMeasurementListener {

    private static final Logger logger = LoggerFactory.getLogger(DeviceMeasurementListener.class);
    private final MonitoredDeviceRepository monitoredDeviceRepository;

    // TODO: după ce testăm, îl luăm din MonitoredDevice / config / device-service
    private static final double MAX_ALLOWED_KWH_PER_HOUR = 10.0;

    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final OverconsumptionAlertPublisher alertPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceMeasurementListener(
            HourlyConsumptionRepository hourlyConsumptionRepository,
            OverconsumptionAlertPublisher alertPublisher,
            MonitoredDeviceRepository monitoredDeviceRepository
    ) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.alertPublisher = alertPublisher;
        this.monitoredDeviceRepository = monitoredDeviceRepository;
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

            UUID deviceId = UUID.fromString(message.getDeviceId());

            OffsetDateTime odt = OffsetDateTime.parse(message.getTimestamp());
            LocalDateTime localTimestamp = odt.atZoneSameInstant(ZoneId.of("Europe/Bucharest")).toLocalDateTime();

            LocalDateTime hourStart = localTimestamp
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            Double value = message.getMeasurementValue();
            if (value == null) {
                logger.warn("Measurement value is null, ignoring message");
                return;
            }

            // 1) upsert HourlyConsumption
            HourlyConsumption saved = upsertHourlyConsumption(deviceId, hourStart, value);

            logger.error("DEBUG: after upsert. deviceId={}, hourStart={}, total={}",
                    deviceId, hourStart, saved.getEnergyKwh());


            // 2) detect + send alert (automat!)
            double totalKwhThisHour = saved.getEnergyKwh();

            logger.error("DEBUG: comparing total={} with threshold={}",
                    saved.getEnergyKwh(), MAX_ALLOWED_KWH_PER_HOUR);

            if (totalKwhThisHour > MAX_ALLOWED_KWH_PER_HOUR) {

                logger.error("DEBUG: ENTERED ALERT IF !!!");


                MonitoredDevice device = monitoredDeviceRepository
                        .findById(deviceId)
                        .orElseThrow(() -> new RuntimeException("Device not found"));

                OverconsumptionAlertDTO alert = new OverconsumptionAlertDTO(
                        device.getUserId().toString(),
                        deviceId.toString(),
                        totalKwhThisHour,
                        device.getMaxHourlyConsumption()
                );

                alertPublisher.publish(alert);

                alertPublisher.publish(alert);

                logger.warn("OVERCONSUMPTION ALERT SENT: deviceId={}, hourStart={}, consumption={}, threshold={}",
                        deviceId, hourStart, totalKwhThisHour, MAX_ALLOWED_KWH_PER_HOUR);
            }

        } catch (Exception e) {
            logger.error("Error while processing measurement message", e);
        }
    }

    private HourlyConsumption upsertHourlyConsumption(UUID deviceId, LocalDateTime hourStart, double value) {
        Optional<HourlyConsumption> existingOpt =
                hourlyConsumptionRepository.findByDeviceIdAndHourStart(deviceId, hourStart);

        if (existingOpt.isPresent()) {
            HourlyConsumption existing = existingOpt.get();
            existing.setEnergyKwh(existing.getEnergyKwh() + value);
            HourlyConsumption saved = hourlyConsumptionRepository.save(existing);

            logger.info("Updated hourly consumption: deviceId={}, hourStart={}, newEnergy={}",
                    deviceId, hourStart, saved.getEnergyKwh());

            return saved;
        } else {
            HourlyConsumption newEntry = new HourlyConsumption(deviceId, hourStart, value);
            HourlyConsumption saved = hourlyConsumptionRepository.save(newEntry);

            logger.info("Created new hourly consumption: deviceId={}, hourStart={}, energy={}",
                    deviceId, hourStart, value);

            return saved;
        }
    }
}
