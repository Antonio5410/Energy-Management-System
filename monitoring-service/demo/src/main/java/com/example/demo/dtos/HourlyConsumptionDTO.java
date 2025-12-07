package com.example.demo.dtos;

import java.time.LocalDateTime;

public class HourlyConsumptionDTO {

    private LocalDateTime hourStart;
    private double energyKwh;

    public HourlyConsumptionDTO(LocalDateTime hourStart, double energyKwh) {
        this.hourStart = hourStart;
        this.energyKwh = energyKwh;
    }

    public LocalDateTime getHourStart() {
        return hourStart;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }
}
