package com.example.demo.dtos;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class SyncEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;      // ex: "USER_CREATED"
    private UUID userId;
    private String username;
    private Instant timestamp;

    public SyncEventDTO() {
    }

    // getters & setters

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
}
