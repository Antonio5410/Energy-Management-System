package com.example.demo.services;

import com.example.demo.dtos.DailyConsumptionDto;
import com.example.demo.entities.HourlyConsumption;
import com.example.demo.repositories.HourlyConsumptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MonitoringService {

    private final HourlyConsumptionRepository hourlyConsumptionRepository;

    public MonitoringService(HourlyConsumptionRepository hourlyConsumptionRepository) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
    }

    public List<DailyConsumptionDto> getDailyConsumption(UUID deviceId, LocalDate date) {
        // [date 00:00, date+1 00:00)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDayExclusive = startOfDay.plusDays(1);

        List<HourlyConsumption> records =
                hourlyConsumptionRepository.findByDeviceIdAndHourStartBetween(
                        deviceId,
                        startOfDay,
                        endOfDayExclusive
                );

        // inițializăm 24 de ore cu 0
        double[] perHour = new double[24];

        for (HourlyConsumption hc : records) {
            int hour = hc.getHourStart().getHour(); // presupunem getHourStart() in entity
            perHour[hour] += hc.getEnergyKwh();     // și getEnergyKwh()
        }

        List<DailyConsumptionDto> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            result.add(new DailyConsumptionDto(h, perHour[h]));
        }

        return result;
    }
}
