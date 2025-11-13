package com.example.demo.dtos;


import com.example.demo.dtos.validators.annotation.AgeLimit;
import com.example.demo.entities.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PersonDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "address is required")
    private String address;
    @NotNull(message = "age is required")
    @AgeLimit(value = 18)
    private Integer age;
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "password is required")
    private String password;
    @NotNull(message = "role is required")
    private Role role;

    public PersonDetailsDTO() {
    }

    public PersonDetailsDTO( String name, String address, int age, String username, String password, Role role) {
        this.name = name;
        this.address = address;
        this.age = age;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public PersonDetailsDTO(UUID id, String name, String address, int age, String username, String password, Role role) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.age = age;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public Role getRole() {return role;}
    public void setRole(Role role) {this.role = role;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDetailsDTO that = (PersonDetailsDTO) o;
        return age == that.age &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, age);
    }
}
