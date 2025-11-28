package com.example.demo.controllers;

import com.example.demo.dtos.DailyConsumptionDto;
import com.example.demo.entities.HourlyConsumption;
import com.example.demo.repositories.HourlyConsumptionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.example.demo.services.MonitoringService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
public class MonitoringTestController {

    private final MonitoringService monitoringService;

    private final HourlyConsumptionRepository hourlyConsumptionRepository;

    public MonitoringTestController(MonitoringService monitoringService, HourlyConsumptionRepository hourlyConsumptionRepository) {
        this.monitoringService = monitoringService;
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
    }

    @GetMapping("/test")
    public List<HourlyConsumption> testDb() {
        // Inserăm un row de test, ca să fim siguri că putem SCRIE
        HourlyConsumption hc = new HourlyConsumption(
                UUID.randomUUID(),                    // deviceId random, doar de test
                LocalDateTime.now().withMinute(0).withSecond(0).withNano(0), // început de oră
                1.23                                  // energie kWh de test
        );

        hourlyConsumptionRepository.save(hc);

        // Returnăm toate înregistrările, ca să vedem că putem și CITI
        return hourlyConsumptionRepository.findAll();
    }

    // GET /monitoring/devices/{deviceId}/daily?date=2025-11-27
    @GetMapping("/devices/{deviceId}/daily")
    public List<DailyConsumptionDto> getDailyConsumption(
            @PathVariable UUID deviceId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return monitoringService.getDailyConsumption(deviceId, date);
    }
}

