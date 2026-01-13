package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @PostConstruct
    public void debugSecret() {
        System.out.println("PEOPLE-SERVICE JWT SECRET = " + secret);
    }

    @Value("${app.jwt.secret:${APP_JWT_SECRET}}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        Claims claims = parseClaims(token);
        Date exp = claims.getExpiration();
        return exp != null && exp.after(new Date());
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    public String extractUserId(String token) {
        Object userId = parseClaims(token).get("userId");
        return userId != null ? userId.toString() : null;
    }
}
