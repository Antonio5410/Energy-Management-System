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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${people.service.url}")
    private String peopleServiceUrl;

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
        String url = peopleServiceUrl + "/" + personId;
        System.out.println("Calling people-service: " + url);

        try {
            var response = restTemplate.getForEntity(url, Void.class);
            System.out.println("people-service responded with: " + response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();

        } catch (HttpClientErrorException.NotFound e) {
            // 404 -> userul NU exista
            System.out.println("people-service returned 404 NOT FOUND for " + personId);
            return false;

        } catch (HttpClientErrorException e) {
            // alte 4xx / 5xx – pentru proiectul asta le tratam tot ca "nu exista"
            System.out.println("people-service returned error status: " + e.getStatusCode());
            return false;

        } catch (RestClientException e) {
            // conexiune picata, DNS gresit, etc.
            System.out.println("Error calling people-service: " + e.getMessage());
            return false;
        }
    }

    public List<DeviceDTO> findDevicesByOwner(UUID ownerId) {
        if (!userExists(ownerId)) {
            throw new ResourceNotFoundException(
                    "The userId = " + ownerId.toString() + " was not found."
            );
        }

        List<Device> devices = deviceRepository.findByOwnerId(ownerId);

        return devices.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
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

    public DeviceDetailsDTO update(UUID id, DeviceDetailsDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "This device does not exist."
                ));

        if (dto.getOwnerId() == null) {
            throw new ResourceNotFoundException(
                    "The userId cannot be null."
            );
        }

        if (!userExists(dto.getOwnerId())) {
            throw new ResourceNotFoundException(
                    "The userId is invalid."
            );
        }

        device.setName(dto.getName());
        device.setConsumMaxim(dto.getConsumMaxim());
        device.setOwnerId(dto.getOwnerId());

        return DeviceBuilder.toDeviceDetailsDTO(deviceRepository.save(device));
    }

    public void delete(UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        deviceRepository.deleteById(id);
    }
    public void deleteDevicesByOwnerId(UUID ownerId) {
        List<Device> devices = deviceRepository.findByOwnerId(ownerId);
        System.out.println("Cascade delete: found " + devices.size() + " device(s) for ownerId = " + ownerId);
        deviceRepository.deleteAll(devices);
    }

}
