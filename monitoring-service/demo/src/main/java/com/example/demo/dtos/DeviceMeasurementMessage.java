package com.example.demo.dtos;

public class DeviceMeasurementMessage {

    private String timestamp;       // ex: "2025-11-26T15:40:00Z"
    private String deviceId;        // UUID ca string
    private Double measurementValue;

    public DeviceMeasurementMessage() {
    }

    public DeviceMeasurementMessage(String timestamp, String deviceId, Double measurementValue) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.measurementValue = measurementValue;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(Double measurementValue) {
        this.measurementValue = measurementValue;
    }

    @Override
    public String toString() {
        return "DeviceMeasurementMessage{" +
                "timestamp='" + timestamp + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", measurementValue=" + measurementValue +
                '}';
    }
}
