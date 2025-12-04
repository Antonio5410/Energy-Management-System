package com.example.demo.repositories;

import com.example.demo.entities.MonitoredDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonitoredDeviceRepository extends JpaRepository<MonitoredDevice, UUID> {
}
