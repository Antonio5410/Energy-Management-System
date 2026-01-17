package com.example.demo.controllers;

import com.example.demo.entities.Role;
import com.example.demo.services.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class InternalSyncController {

    private final AuthService authService;

    @Value("${internal.secret}")
    private String internalSecret;

    public InternalSyncController(AuthService authService) {
        this.authService = authService;
    }

    public static class UpdateCredentialsRequest {
        private String username;
        private Role role; // CLIENT / ADMIN

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    private boolean isAuthorized(String headerSecret) {
        return headerSecret != null && headerSecret.equals(internalSecret);
    }

    @PutMapping("/credentials/{userId}")
    public ResponseEntity<?> updateByUserId(@PathVariable UUID userId,
                                            @RequestBody UpdateCredentialsRequest req,
                                            @RequestHeader(value = "X-INTERNAL-SECRET", required = false) String headerSecret) {

        if (!isAuthorized(headerSecret)) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        authService.syncUpdateCredentials(userId, req.getUsername(), req.getRole());
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @DeleteMapping("/credentials/{userId}")
    public ResponseEntity<?> deleteByUserId(@PathVariable UUID userId,
                                            @RequestHeader(value = "X-INTERNAL-SECRET", required = false) String headerSecret) {

        if (!isAuthorized(headerSecret)) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        authService.syncDeleteCredentials(userId);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}
