package com.example.demo.services;

import com.example.demo.dtos.CredentialsDTO;
import com.example.demo.dtos.builders.CredentialsBuilder;
import com.example.demo.entities.Credentials;
import com.example.demo.entities.Role;
import com.example.demo.repositories.CredentialsRepository;
import com.example.demo.security.CredentialUserDetails;
import com.example.demo.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       CredentialsRepository credentialsRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.credentialsRepository = credentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CredentialsDTO register(String username, String password, Role role, UUID userId) {
        Optional<Credentials> existing = credentialsRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Credentials cred = new Credentials();
        cred.setUsername(username);
        cred.setPassword(passwordEncoder.encode(password));
        cred.setRole(role != null ? role : Role.CLIENT);

        // IMPORTANT: userId vine de la people-service; dacă nu există încă, îl lași null.
        cred.setUserId(userId);

        Credentials saved = credentialsRepository.save(cred);
        return CredentialsBuilder.toCredentialsDTO(saved);
    }


    public Map<String, Object> login(String username, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            CredentialUserDetails userDetails = (CredentialUserDetails) auth.getPrincipal();
            Credentials cred = userDetails.getCredentialsEntity();
            Role role = cred.getRole();

            String safeUserId = (cred.getUserId() != null)
                    ? cred.getUserId().toString()
                    : cred.getId().toString();

            String token = jwtService.generateToken(
                    cred.getUsername(),
                    role,
                    safeUserId
            );

            return Map.of(
                    "token", token,
                    "username", cred.getUsername(),
                    "role", role.name(),
                    "userId", safeUserId
            );

        } catch (BadCredentialsException e) {
            throw e;
        }
    }
    public void syncUserUpdated(UUID userId, String newUsername, String roleStr) {
        if (userId == null) return;

        Credentials cred = credentialsRepository.findByUserId(userId)
                .orElse(null);

        // dacă userul nu există în auth, nu facem nimic (nu vrem cred fără parolă)
        if (cred == null) return;

        if (newUsername != null && !newUsername.isBlank()) {
            // username e UNIQUE -> dacă există altul cu același username, decizi ce faci
            // safe: dacă e luat de altcineva, ignor update-ul username
            var conflict = credentialsRepository.findByUsername(newUsername);
            if (conflict.isEmpty() || conflict.get().getId().equals(cred.getId())) {
                cred.setUsername(newUsername);
            }
        }

        if (roleStr != null && !roleStr.isBlank()) {
            try {
                cred.setRole(Role.valueOf(roleStr));
            } catch (IllegalArgumentException ignored) {
                // role invalid -> ignore
            }
        }

        credentialsRepository.save(cred);
    }

    public void syncUserDeleted(UUID userId) {
        if (userId == null) return;
        credentialsRepository.deleteByUserId(userId);
    }

    public void syncUpdateCredentials(UUID userId, String newUsername, Role newRole) {
        Credentials cred = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No credentials for userId=" + userId));

        // dacă se schimbă username-ul, verificăm coliziuni
        if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(cred.getUsername())) {
            Optional<Credentials> existing = credentialsRepository.findByUsername(newUsername);
            if (existing.isPresent() && !existing.get().getId().equals(cred.getId())) {
                throw new IllegalArgumentException("Username already exists: " + newUsername);
            }
            cred.setUsername(newUsername);
        }

        if (newRole != null) {
            cred.setRole(newRole);
        }

        credentialsRepository.save(cred);
    }

    public void syncDeleteCredentials(UUID userId) {
        // dacă nu există -> ok, nu crăpăm
        credentialsRepository.findByUserId(userId).ifPresent(c -> credentialsRepository.delete(c));
    }


}
