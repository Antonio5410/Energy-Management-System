package com.example.demo.services;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.dtos.SyncEventDTO;
import com.example.demo.dtos.builders.PersonBuilder;
import com.example.demo.entities.Person;
import com.example.demo.handlers.exceptions.model.DuplicateResourceException;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${device.service.url}")
    private String deviceServiceUrl;

    @Value("${sync.exchange}")
    private String syncExchange;

    @Value("${sync.routing-key}")
    private String syncRoutingKey;

    // ✅ UN SINGUR CONSTRUCTOR (și nu depinzi de bean RestTemplate)
    public PersonService(PersonRepository personRepository, RabbitTemplate rabbitTemplate) {
        this.personRepository = personRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = new RestTemplate();
    }

    public List<PersonDTO> findPersons() {
        List<Person> personList = personRepository.findAll();
        return personList.stream()
                .map(PersonBuilder::toPersonDTO)
                .collect(Collectors.toList());
    }

    public PersonDetailsDTO findPersonById(UUID id) {
        Optional<Person> personOptional = personRepository.findById(id);
        if (personOptional.isEmpty()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        return PersonBuilder.toPersonDetailsDTO(personOptional.get());
    }

    public UUID insert(PersonDetailsDTO personDTO) {
        Person person = PersonBuilder.toEntity(personDTO);
        person = personRepository.save(person);
        LOGGER.debug("Person with id {} was inserted in db", person.getId());

        SyncEventDTO event = new SyncEventDTO();
        event.setEventType("USER_CREATED");
        event.setUserId(person.getId());
        event.setUsername(person.getUsername());
        event.setTimestamp(Instant.now());

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncRoutingKey, event);
            LOGGER.info("Sent USER_CREATED sync event for user {}", person.getId());
        } catch (Exception e) {
            LOGGER.warn("Failed to send USER_CREATED sync event for user {}: {}", person.getId(), e.getMessage());
        }

        return person.getId();
    }

    public boolean existsById(UUID id) {
        return personRepository.existsById(id);
    }

    // ✅ pentru /people/internal/id-by-username/{username}
    public UUID findIdByUsername(String username) {
        return personRepository.findByUsername(username)
                .map(Person::getId)
                .orElse(null);
    }

    public UUID update(UUID id, PersonDetailsDTO personDTO) {
        Optional<Person> personOptional = personRepository.findById(id);
        if (personOptional.isEmpty()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }

        Person person = personOptional.get();

        Optional<Person> existingUser = personRepository.findByUsername(personDTO.getUsername());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            LOGGER.error("Username {} already exists", personDTO.getUsername());
            throw new DuplicateResourceException("Username already exists: " + personDTO.getUsername());
        }

        person.setUsername(personDTO.getUsername());

        if (personDTO.getPassword() != null && !personDTO.getPassword().isBlank()) {
            person.setPassword(personDTO.getPassword());
        }

        person.setRole(personDTO.getRole());
        person.setName(personDTO.getName());
        person.setAddress(personDTO.getAddress());
        person.setAge(personDTO.getAge());

        personRepository.save(person);
        LOGGER.debug("Person with id {} was updated in db", id);

        return id;
    }

    private void deleteDevicesForPerson(UUID personId) {
        String url = deviceServiceUrl + "/owner/" + personId;
        System.out.println("Cascade delete: GET " + url);

        try {
            DeviceDTO[] devicesArray = restTemplate.getForObject(url, DeviceDTO[].class);

            if (devicesArray == null) {
                System.out.println("Cascade delete: no devices found (null body) for " + personId);
                return;
            }

            List<DeviceDTO> devices = Arrays.asList(devicesArray);
            System.out.println("Cascade delete: found " + devices.size() + " device(s) for " + personId);

            for (DeviceDTO device : devices) {
                String deleteUrl = deviceServiceUrl + "/" + device.getId();
                System.out.println("Cascade delete: DELETE " + deleteUrl);
                restTemplate.delete(deleteUrl);
            }

        } catch (RestClientException e) {
            System.out.println("Cascade delete FAILED for person " + personId + ": " + e.getMessage());
        }
    }

    public void delete(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find the person with id: " + id));

        deleteDevicesForPerson(id);

        personRepository.delete(person);
        LOGGER.debug("Person with id {} was deleted from db", id);
    }

}
