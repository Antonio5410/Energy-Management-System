package com.example.demo.dtos.builders;

import com.example.demo.dtos.CredentialsDTO;
import com.example.demo.entities.Credentials;

public class CredentialsBuilder {

    private CredentialsBuilder() {
    }

    public static CredentialsDTO toCredentialsDTO(Credentials credentials) {
        if (credentials == null) {
            return null;
        }
        return new CredentialsDTO(
                credentials.getId(),
                credentials.getUsername(),
                credentials.getRole(),
                credentials.getUserId()
        );
    }
}
