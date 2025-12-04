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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${device.service.url}")
    private String deviceServiceUrl;

    @Value("${sync.exchange}")        // ex: sync.exchange
    private String syncExchange;

    @Value("${sync.routing-key}")     // ex: sync.key
    private String syncRoutingKey;


    @Autowired
    public PersonService(PersonRepository personRepository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = restTemplate;
        this.personRepository = personRepository;
    }

    public List<PersonDTO> findPersons() {
        List<Person> personList = personRepository.findAll();
        return personList.stream()
                .map(PersonBuilder::toPersonDTO)
                .collect(Collectors.toList());
    }

    public PersonDetailsDTO findPersonById(UUID id) {
        Optional<Person> prosumerOptional = personRepository.findById(id);
        if (prosumerOptional.isEmpty()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        return PersonBuilder.toPersonDetailsDTO(prosumerOptional.get());
    }

    public UUID insert(PersonDetailsDTO personDTO) {
//        Person person = PersonBuilder.toEntity(personDTO);
//        person = personRepository.save(person);
//        LOGGER.debug("Person with id {} was inserted in db", person.getId());
//        return person.getId();
        // 1. salvăm user-ul în baza de date
        Person person = PersonBuilder.toEntity(personDTO);
        person = personRepository.save(person);
        LOGGER.debug("Person with id {} was inserted in db", person.getId());

        // 2. construim evenimentul de sincronizare
        SyncEventDTO event = new SyncEventDTO();
        event.setEventType("USER_CREATED");
        event.setUserId(person.getId());
        event.setUsername(person.getUsername());
        event.setTimestamp(Instant.now());

        // 3. trimitem în RabbitMQ
        try {
            rabbitTemplate.convertAndSend(syncExchange, syncRoutingKey, event);
            LOGGER.info("Sent USER_CREATED sync event for user {}", person.getId());
        } catch (Exception e) {
            // aici alegi filozofia:
            // - dacă vrei să NU oprești crearea userului când pică RabbitMQ, doar loghezi:
            LOGGER.warn("Failed to send USER_CREATED sync event for user {}: {}", person.getId(), e.getMessage());
            // - dacă vrei să pici tot (user + sync) când RabbitMQ nu merge, arunci RuntimeException
            // throw new RuntimeException("Failed to send sync event", e);
        }

        return person.getId();
    }

    public UUID update(UUID id, PersonDetailsDTO personDTO) {
        Optional<Person> personOptional = personRepository.findById(id);
        if (personOptional.isEmpty()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }

        Person person = personOptional.get();

        // Verificăm dacă username-ul e deja luat de alt user
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
            // la proiectul tău eu aș lăsa doar logul și *aș continua* ștergerea persoanei
            // dacă vrei să NU ștergi persoana când pică device-service, aici arunci RuntimeException
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
