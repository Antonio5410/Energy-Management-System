package com.example.demo.services;


import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.handlers.exceptions.model.DuplicateResourceException;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.demo.dtos.builders.DeviceBuilder.toDeviceDTO;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<DeviceDTO> findDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> deviceOpt = deviceRepository.findById(id);
        if (!deviceOpt.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(deviceOpt.get());
    }

//    List<DeviceDetailsDTO> findDevicesByOwnerId(UUID ownerId) {
//        Optional<Device> deviceOpt = deviceRepository.findById(ownerId);
//        if (!deviceOpt.isPresent()) {
//            LOGGER.error("Device with owner id {} was not found in db", ownerId);
//            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + ownerId);
//        }
//        return
//    }

//    List<DeviceDetailsDTO> findDevicesByOwnerId(UUID ownerId) {
//        List<Device> devices = deviceRepository.findByOwnerId(ownerId);
//        if (devices.isEmpty()) {
//            LOGGER.error("Devices with owner id {} were not found in db", ownerId);
//            throw new ResourceNotFoundException(Device.class.getSimpleName() + "s with owner id: " + ownerId);
//        }
//        return devices.stream()
//                .map(DeviceBuilder::toDeviceDetailsDTO)
//                .collect(Collectors.toList());
//    }

    public List<DeviceDTO> getDevicesByOwner(UUID ownerId) {
        return deviceRepository.findByOwnerId(ownerId).stream()
                .map(this::toDto)
                .toList();
    }
    private DeviceDTO toDto(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setOwnerId(device.getOwnerId());
        return dto;
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    public DeviceDetailsDTO update(UUID id, DeviceDetailsDTO updated) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id));
        device.setName(updated.getName());
        device.setConsumMaxim(updated.getConsumMaxim());
        device.setOwnerId(updated.getOwnerId());
        return DeviceBuilder.toDeviceDetailsDTO(deviceRepository.save(device));
    }

    public void delete(UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        deviceRepository.deleteById(id);
    }

}
