package com.example.demo.devicedatasimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeviceDataSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceDataSimulatorApplication.class, args);
    }

}
