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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final RestTemplate restTemplate;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
        this.restTemplate = new RestTemplate();
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

    private boolean userExists(UUID personId) {
        String peopleServiceUrl = "http://people-service:8081/people/";
        String url = peopleServiceUrl + personId;
        try {
            var response = restTemplate.getForEntity(url, Void.class);
            // dacă ajungem aici și e 2xx, userul există
            return response.getStatusCode().is2xxSuccessful();

        } catch (HttpClientErrorException.NotFound e) {
            // 404 -> userul NU există
            return false;

        } catch (HttpClientErrorException e) {
            // alte 4xx/5xx (401, 403, 500 etc.) -> le tratăm ca "nu există" pentru simplitate
            System.out.println("people-service returned status: " + e.getStatusCode());
            return false;

        } catch (RestClientException e) {
            // conexiune picată, hostname greșit etc.
            System.out.println("Error calling people-service: " + e.getMessage());
            return false;
        }
    }


    public UUID insert(DeviceDetailsDTO deviceDTO) {

        if (deviceDTO.getOwnerId() == null) {
            throw new ResourceNotFoundException("The user ID cannot be null.");
        }

        if (!userExists(deviceDTO.getOwnerId())) {
            throw new ResourceNotFoundException("The user ID was not found.");
        }

        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    public void delete(UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        deviceRepository.deleteById(id);
    }

    public DeviceDetailsDTO update(UUID id, DeviceDetailsDTO updated) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id));
        device.setName(updated.getName());
        device.setConsumMaxim(updated.getConsumMaxim());
        return DeviceBuilder.toDeviceDetailsDTO(deviceRepository.save(device));
    }


}
