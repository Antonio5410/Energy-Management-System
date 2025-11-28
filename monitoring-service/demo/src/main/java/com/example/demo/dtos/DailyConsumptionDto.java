package com.example.demo.dtos;

public class DailyConsumptionDto {

    private int hour;          // 0..23
    private double energyKwh;  // consumul pe ora respectiva

    public DailyConsumptionDto() {
    }

    public DailyConsumptionDto(int hour, double energyKwh) {
        this.hour = hour;
        this.energyKwh = energyKwh;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(double energyKwh) {
        this.energyKwh = energyKwh;
    }
}
