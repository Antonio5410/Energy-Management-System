package com.example.demo.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;


@Entity
@Table(name = "devices")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "consumMaxim", nullable = false)
    private Integer consumMaxim;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    public Device() {
    }

    public Device(String name, Integer consumMaxim, UUID ownerId) {
        this.name = name;
        this.consumMaxim = consumMaxim;
        this.ownerId = ownerId;
    }

    public UUID getId(){return this.id;}
    public void setId(UUID id){this.id = id;}

    public Integer getConsumMaxim(){return this.consumMaxim;}
    public void setConsumMaxim(Integer consumMaxim){this.consumMaxim = consumMaxim;}

    public String getName(){return this.name;}
    public void setName(String name){this.name = name;}

    public UUID getOwnerId(){return this.ownerId;}
    public void setOwnerId(UUID ownerId){this.ownerId = ownerId;}
}
