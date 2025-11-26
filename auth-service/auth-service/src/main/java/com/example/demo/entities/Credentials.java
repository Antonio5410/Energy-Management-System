package com.example.demo.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "credentials")
public class Credentials {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // stocată BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // legătura cu user-ul din people-service (id-ul lui)
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
