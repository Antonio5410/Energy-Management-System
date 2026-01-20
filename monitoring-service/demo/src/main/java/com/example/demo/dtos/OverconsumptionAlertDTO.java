package com.example.demo.dtos;


public class OverconsumptionAlertDTO {

    private String userId;
    private String deviceId;
    private double value;
    private double maxAllowed;

    public OverconsumptionAlertDTO() {}

    public OverconsumptionAlertDTO(String userId, String deviceId,
                                   double value, double maxAllowed) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.value = value;
        this.maxAllowed = maxAllowed;
    }

    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public double getValue() { return value; }
    public double getMaxAllowed() { return maxAllowed; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setValue(double value) { this.value = value; }
    public void setMaxAllowed(double maxAllowed) { this.maxAllowed = maxAllowed; }
}
