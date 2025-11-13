package com.example.demo.dtos;

import com.example.demo.entities.enums.Role;

import java.util.Objects;
import java.util.UUID;

public class PersonDTO {
    private UUID id;
    private String username;
    private Role role;
    private String name;
    private String address;
    private int age;

    public PersonDTO() {}
    public PersonDTO(UUID id, String username, Role role, String name, String address, int age) {
        this.id = id; this.name = name; this.age = age;
        this.username = username;
        this.role = role;
        this.name = name; this.address = address;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }



    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDTO that = (PersonDTO) o;
        return age == that.age && Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(role, that.role) && Objects.equals(address, that.address);
    }
    @Override public int hashCode() { return Objects.hash(name, age); }
}
