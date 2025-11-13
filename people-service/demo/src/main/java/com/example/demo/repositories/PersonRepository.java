package com.example.demo.repositories;

import com.example.demo.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    /**
     * Example: JPA generate query by existing field
     */
    List<Person> findByName(String name);

    Optional<Person> findByUsername(String username);

    boolean existsByUsername(String username);


    /**
     * Example: Custom query
     */
    @Query(value = "SELECT p " +
            "FROM Person p " +
            "WHERE p.name = :name " +
            "AND p.age >= 60  ")
    Optional<Person> findSeniorsByName(@Param("name") String name);

}
