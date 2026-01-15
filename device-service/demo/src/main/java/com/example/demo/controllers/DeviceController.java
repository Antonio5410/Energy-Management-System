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

    // ADMIN -> toate; CLIENT -> doar ale lui
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (isAdmin) {
            return ResponseEntity.ok(deviceService.findDevices());
        }

        // CLIENT: ownerId fie din token (dacă există), fie din people-service după username
        UUID ownerId = resolveClientOwnerId(principal);
        return ResponseEntity.ok(deviceService.findDevicesByOwner(ownerId));
    }

    // ADMIN -> orice; CLIENT -> doar dacă e al lui
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id,
                                                      Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (isAdmin) {
            return ResponseEntity.ok(deviceService.findDeviceById(id));
        }

        UUID ownerId = resolveClientOwnerId(principal);
        if (!deviceService.isDeviceOwnedBy(id, ownerId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    // CREATE: CLIENT -> owner forțat; ADMIN -> poate trimite ownerId
    @PostMapping
    public ResponseEntity<UUID> create(@Valid @RequestBody DeviceDetailsDTO device,
                                       Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());
        boolean isClient = "CLIENT".equals(principal.getRole());

        if (!isAdmin && !isClient) {
            return ResponseEntity.status(403).build();
        }

        if (isClient) {
            UUID ownerId = resolveClientOwnerId(principal);
            device.setOwnerId(ownerId); // ignoră ce trimite clientul
        } else {
            // admin trebuie să trimită ownerId (altfel nu știm pentru cine creează)
            if (device.getOwnerId() == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        UUID id = deviceService.insert(device);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    // UPDATE/DELETE: doar ADMIN (ca să terminăm rapid)
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> updateDevice(@PathVariable UUID id,
                                                         @Valid @RequestBody DeviceDetailsDTO device,
                                                         Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        if (!"ADMIN".equals(principal.getRole())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(deviceService.update(id, device));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id,
                                             Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        if (!"ADMIN".equals(principal.getRole())) {
            return ResponseEntity.status(403).build();
        }

        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveClientOwnerId(JwtPrincipal principal) {
        // dacă ai userId în token, îl folosești
        if (principal.getUserId() != null && !principal.getUserId().isBlank()) {
            return UUID.fromString(principal.getUserId());
        }
        // altfel, îl rezolvi după username din people-service
        return deviceService.resolveOwnerIdByUsername(principal.getUsername());
    }
}

