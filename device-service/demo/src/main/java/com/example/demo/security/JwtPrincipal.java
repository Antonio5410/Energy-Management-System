package com.example.demo.security;

public class JwtPrincipal {

    private final String username;
    private final String userId;
    private final String role;

    public JwtPrincipal(String username, String userId, String role) {
        this.username = username;
        this.userId = userId;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
