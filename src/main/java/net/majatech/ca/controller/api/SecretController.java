package net.majatech.ca.controller.api;

import net.majatech.ca.services.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/secret")
public class SecretController {

    private final SecretService secretService;

    @Autowired
    public SecretController(SecretService secretService) {
        this.secretService = secretService;
    }

    /**
     * Endpoint to initiate the call to the elsewhere hosted secret API and fetch the protected data
     * @param keyStoreId The ID of the KeyStore to use when creating the SSLContext for connection to the secret API
     * @return The secret in text format in the request body. Errors are handled by the JS for better demonstration
     */
    @GetMapping(value = "/{keyStoreId}")
    public String fetchSecrets(@PathVariable("keyStoreId") UUID keyStoreId) {
        return secretService.fetchSecrets(keyStoreId);
    }
}
