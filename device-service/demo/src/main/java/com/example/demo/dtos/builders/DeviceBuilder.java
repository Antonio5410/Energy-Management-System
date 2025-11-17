package com.example.demo.dtos.builders;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(device.getId(), device.getName(), device.getOwnerId());
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(device.getId(), device.getName(), device.getConsumMaxim(), device.getOwnerId());
    }

    public static Device toEntity(DeviceDetailsDTO deviceDTO) {
        return new Device(deviceDTO.getName(), deviceDTO.getConsumMaxim(), deviceDTO.getOwnerId());
    }
}
