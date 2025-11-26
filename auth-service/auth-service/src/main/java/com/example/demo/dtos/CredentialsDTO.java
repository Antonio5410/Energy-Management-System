package com.example.demo.dtos;

import com.example.demo.entities.Role;

import java.util.UUID;

public class CredentialsDTO {

    private UUID id;
    private String username;
    private Role role;
    private UUID userId;

    public CredentialsDTO() {
    }

    public CredentialsDTO(UUID id, String username, Role role, UUID userId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
