package com.example.demo.dtos;

import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private int consumMaxim;
    private UUID ownerId;

    public DeviceDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getConsumMaxim() { return consumMaxim; }
    public void setConsumMaxim(int consumMaxim) { this.consumMaxim = consumMaxim; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
}
