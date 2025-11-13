package com.example.demo.controllers;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.services.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/people")
@Validated
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public ResponseEntity<List<PersonDTO>> getPeople() {
        return ResponseEntity.ok(personService.findPersons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailsDTO> getPerson(@PathVariable UUID id) {
        return ResponseEntity.ok(personService.findPersonById(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody PersonDetailsDTO person) {
        UUID id = personService.insert(person);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build(); // 201 + Location header
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody PersonDetailsDTO person) {
        personService.update(id, person);
        return ResponseEntity.noContent().build(); // 204
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        personService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
