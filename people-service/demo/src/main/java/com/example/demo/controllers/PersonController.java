package com.example.demo.controllers;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.security.JwtPrincipal;
import com.example.demo.services.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // ADMIN only
    @GetMapping
    public ResponseEntity<List<PersonDTO>> getPeople(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(personService.findPersons());
    }

    // ADMIN or OWNER (client can see only his own profile)
    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailsDTO> getPerson(@PathVariable UUID id,
                                                      Authentication authentication) {

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();

        boolean isAdmin = "ADMIN".equals(principal.getRole());
        boolean isOwner = principal.getUserId() != null && principal.getUserId().equals(id.toString());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(personService.findPersonById(id));
    }

    // ADMIN only
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody PersonDetailsDTO person,
                                       Authentication authentication) {

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        UUID id = personService.insert(person);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // ADMIN or OWNER
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @Valid @RequestBody PersonDetailsDTO person,
                                       Authentication authentication) {

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();

        boolean isAdmin = "ADMIN".equals(principal.getRole());
        boolean isOwner = principal.getUserId() != null && principal.getUserId().equals(id.toString());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403).build();
        }

        personService.update(id, person);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/internal/exists/{id}")
    public ResponseEntity<Void> exists(@PathVariable UUID id) {
        boolean ok = personService.existsById(id);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       Authentication authentication) {

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        boolean isAdmin = "ADMIN".equals(principal.getRole());

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        personService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
