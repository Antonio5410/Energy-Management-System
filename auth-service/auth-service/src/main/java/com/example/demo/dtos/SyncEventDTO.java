package com.example.demo.dtos;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class SyncEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // USER_CREATED / USER_UPDATED / USER_DELETED / DEVICE_CREATED etc.
    private String eventType;

    // user fields
    private UUID userId;
    private String username;
    private String role; // <-- ADĂUGAT (CLIENT/ADMIN)

    // device fields
    private UUID deviceId;
    private Double maxHourlyConsumption;

    private Instant timestamp;

    public SyncEventDTO() {}

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public Double getMaxHourlyConsumption() { return maxHourlyConsumption; }
    public void setMaxHourlyConsumption(Double maxHourlyConsumption) { this.maxHourlyConsumption = maxHourlyConsumption; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
