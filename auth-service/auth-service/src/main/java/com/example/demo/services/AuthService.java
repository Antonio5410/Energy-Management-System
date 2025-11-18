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

            String token = jwtService.generateToken(
                    cred.getUsername(),
                    role,
                    cred.getUserId() != null ? cred.getUserId().toString() : ""
            );

            return Map.of(
                    "token", token,
                    "username", cred.getUsername(),
                    "role", role.name(),
                    "userId", cred.getUserId() != null ? cred.getUserId().toString() : null
            );

        } catch (BadCredentialsException e) {
            throw e;
        }
    }
}
