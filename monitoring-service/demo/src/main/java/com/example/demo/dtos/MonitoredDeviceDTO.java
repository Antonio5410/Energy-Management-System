package com.example.demo.dtos;

import java.util.UUID;

public class MonitoredDeviceDTO {

    private UUID id;
    private Double maxHourlyConsumption;

    public MonitoredDeviceDTO(UUID id, Double maxHourlyConsumption) {
        this.id = id;
        this.maxHourlyConsumption = maxHourlyConsumption;
    }

    public UUID getId() {
        return id;
    }

    public Double getMaxHourlyConsumption() {
        return maxHourlyConsumption;
    }
}
