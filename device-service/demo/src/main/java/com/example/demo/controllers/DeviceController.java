package com.example.demo.controllers;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.security.JwtPrincipal;
import com.example.demo.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // ADMIN only (altfel un client ar vedea toate device-urile)
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(deviceService.findDevices());
    }

    // ADMIN only (altfel un client ar putea accesa device by id chiar dacă nu e al lui)
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id,
                                                      Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    // ADMIN or OWNER (client only for himself)
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByOwner(@PathVariable UUID ownerId,
                                                             Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();

        boolean isAdmin = "ADMIN".equals(principal.getRole());
        boolean isOwner = principal.getUserId() != null && principal.getUserId().equals(ownerId.toString());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403).build();
        }

        List<DeviceDTO> devices = deviceService.findDevicesByOwner(ownerId);
        return ResponseEntity.ok(devices);
    }

    @PostMapping
    public ResponseEntity<UUID> create(@Valid @RequestBody DeviceDetailsDTO device,
                                       Authentication authentication) {

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();

        boolean isAdmin = "ADMIN".equals(principal.getRole());
        boolean isClient = "CLIENT".equals(principal.getRole());

        // Client trebuie să aibă userId în token
        if (isClient) {
            if (principal.getUserId() == null || principal.getUserId().isBlank()) {
                return ResponseEntity.status(403).build();
            }

            // Forțezi ownerId = userId din token (ignori ce a trimis clientul)
            device.setOwnerId(UUID.fromString(principal.getUserId()));
        }

        // Dacă nu e admin și nu e client -> interzis
        if (!isAdmin && !isClient) {
            return ResponseEntity.status(403).build();
        }

        System.out.println("CREATE DEVICE ownerId=" + device.getOwnerId());
        UUID id = deviceService.insert(device);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }


    // ADMIN only
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> updateDevice(@PathVariable UUID id,
                                                         @Valid @RequestBody DeviceDetailsDTO device,
                                                         Authentication authentication) {

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(deviceService.update(id, device));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id,
                                             Authentication authentication) {

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
