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

    @GetMapping(value = "/{keyStoreId}")
    public String fetchSecrets(@PathVariable("keyStoreId") UUID keyStoreId) {
        return secretService.fetchSecrets(keyStoreId);
    }
}
