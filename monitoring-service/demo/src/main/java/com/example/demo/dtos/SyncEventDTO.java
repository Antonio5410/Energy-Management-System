package com.example.demo.dtos;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class SyncEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;      // "USER_CREATED" / "DEVICE_CREATED"
    private UUID userId;
    private String username;
    private Instant timestamp;

    private UUID deviceId;
    private Double maxHourlyConsumption;

    public SyncEventDTO() {
    }


    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public Double getMaxHourlyConsumption() {
        return maxHourlyConsumption;
    }

    public void setMaxHourlyConsumption(Double maxHourlyConsumption) {
        this.maxHourlyConsumption = maxHourlyConsumption;
    }
}
