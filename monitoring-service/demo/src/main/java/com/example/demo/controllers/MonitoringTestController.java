package com.example.demo.controllers;

import com.example.demo.entities.HourlyConsumption;
import com.example.demo.repositories.HourlyConsumptionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class MonitoringTestController {

    private final HourlyConsumptionRepository hourlyConsumptionRepository;

    public MonitoringTestController(HourlyConsumptionRepository hourlyConsumptionRepository) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
    }

    @GetMapping("/monitoring/test")
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
}

