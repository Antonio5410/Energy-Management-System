package com.example.demo.dtos;

import java.util.UUID;

public class DeviceDetailsDTO {
    private UUID id;
    private String name;
    private Integer consumMaxim;
    private UUID ownerId;

    public DeviceDetailsDTO() {}

    public DeviceDetailsDTO(UUID id, String name, Integer consumMaxim, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.consumMaxim = consumMaxim;
        this.ownerId = ownerId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getConsumMaxim() { return consumMaxim; }
    public void setConsumMaxim(Integer consumMaxim) { this.consumMaxim = consumMaxim; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
}

