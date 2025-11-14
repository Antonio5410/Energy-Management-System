package com.example.demo.dtos;


import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private UUID ownerId;

    public DeviceDTO() {}

    public DeviceDTO(UUID id, String name, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
}
