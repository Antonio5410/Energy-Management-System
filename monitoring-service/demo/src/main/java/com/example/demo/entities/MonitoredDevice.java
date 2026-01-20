package com.example.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "monitored_device")
public class MonitoredDevice {

    @Id
    private UUID id; // deviceId

    private UUID userId;

    private Double maxHourlyConsumption;

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Double getMaxHourlyConsumption() { return maxHourlyConsumption; }

    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setMaxHourlyConsumption(Double maxHourlyConsumption) {
        this.maxHourlyConsumption = maxHourlyConsumption;
    }
}

