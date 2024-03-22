package net.majatech.ca.services;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecretService {

    public String fetchSecrets(UUID keyStoreId) {
        // Create HTTP Client with provided KeyStore

        // Send request to 'secret' module to fetch secrets

        return "Denied";
    }
}
