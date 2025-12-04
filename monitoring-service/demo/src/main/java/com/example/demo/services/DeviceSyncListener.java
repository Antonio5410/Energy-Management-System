package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.example.demo.dtos.SyncEventDTO;
import com.example.demo.entities.MonitoredDevice;
import com.example.demo.repositories.MonitoredDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DeviceSyncListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSyncListener.class);

    private final MonitoredDeviceRepository monitoredDeviceRepository;

    public DeviceSyncListener(MonitoredDeviceRepository monitoredDeviceRepository) {
        this.monitoredDeviceRepository = monitoredDeviceRepository;
    }

    @RabbitListener(
            queues = RabbitConfig.DEVICE_SYNC_QUEUE,
            containerFactory = "deviceSyncListenerFactory")
    public void handleDeviceCreated(SyncEventDTO event) {
        if (!"DEVICE_CREATED".equals(event.getEventType())) {
            LOGGER.info("Ignoring event of type {}", event.getEventType());
            return;
        }

        MonitoredDevice device = new MonitoredDevice();
        device.setId(event.getDeviceId());
        device.setMaxHourlyConsumption(event.getMaxHourlyConsumption());

        monitoredDeviceRepository.save(device);
        LOGGER.info("Synced device {} into monitoring DB", event.getDeviceId());
    }
}
