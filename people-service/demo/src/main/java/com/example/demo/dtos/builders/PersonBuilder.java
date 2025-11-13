package com.example.demo.dtos.builders;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.entities.Person;

public class PersonBuilder {

    private PersonBuilder() {
    }

    public static PersonDTO toPersonDTO(Person person) {
        //UUID id, String username, Role role, String name, String address, int age
        return new PersonDTO(person.getId(), person.getUsername(), person.getRole(), person.getName(), person.getAddress(), person.getAge());
    }

    public static PersonDetailsDTO toPersonDetailsDTO(Person person) {
        //UUID id, String name, String address, int age, String username, String password, Role role
        return new PersonDetailsDTO(person.getId(), person.getName(), person.getAddress(), person.getAge(),person.getUsername(),person.getPassword(), person.getRole());
    }

    public static Person toEntity(PersonDetailsDTO personDetailsDTO) {
        //String name, String address, int age, String username, String password, Role role
        return new Person(personDetailsDTO.getName(),
                personDetailsDTO.getAddress(),
                personDetailsDTO.getAge(),
                personDetailsDTO.getUsername(),
                personDetailsDTO.getPassword(),
                personDetailsDTO.getRole());
    }
}
