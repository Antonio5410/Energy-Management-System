package com.example.demo.controllers;

import com.example.demo.dtos.HourlyConsumptionDTO;
import com.example.demo.dtos.MonitoredDeviceDTO;
import com.example.demo.entities.HourlyConsumption;
import com.example.demo.repositories.HourlyConsumptionRepository;
import com.example.demo.repositories.MonitoredDeviceRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoredDeviceRepository monitoredDeviceRepository;
    private final HourlyConsumptionRepository hourlyConsumptionRepository;

    public MonitoringController(MonitoredDeviceRepository monitoredDeviceRepository,
                                HourlyConsumptionRepository hourlyConsumptionRepository) {
        this.monitoredDeviceRepository = monitoredDeviceRepository;
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
    }

    // 1) Lista tuturor device-urilor monitorizate
    @GetMapping("/devices")
    public List<MonitoredDeviceDTO> getMonitoredDevices() {
        return monitoredDeviceRepository.findAll()
                .stream()
                .map(device -> new MonitoredDeviceDTO(
                        device.getId(),
                        device.getMaxHourlyConsumption()
                ))
                .collect(Collectors.toList());
    }

    // 2) Consumul orar pentru un device într-un interval [from, to]
    // from / to se trimit ca yyyy-MM-dd (ex: 2025-12-02)
    @GetMapping("/devices/{deviceId}/consumption")
    public List<HourlyConsumptionDTO> getDeviceConsumption(
            @PathVariable UUID deviceId,
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to
    ) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        // luăm până la sfârșitul zilei "to" (exclusiv ziua următoare)
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<HourlyConsumption> consumptions =
                hourlyConsumptionRepository.findByDeviceIdAndHourStartBetweenOrderByHourStartAsc(
                        deviceId,
                        fromDateTime,
                        toDateTime
                );

        return consumptions.stream()
                .map(hc -> new HourlyConsumptionDTO(
                        hc.getHourStart(),
                        hc.getEnergyKwh()
                ))
                .collect(Collectors.toList());
    }
}
