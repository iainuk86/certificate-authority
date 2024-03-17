package net.majatech.secret.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Protected controller that must be accessed using a client certificate signed by our CA.
 * Contains confidential secrets and controversial answers.
 */
@RestController
@RequestMapping("/api/secrets")
public class SuperSecretController {

    @GetMapping
    public ResponseEntity<String> getSecrets() {
        return ResponseEntity.ok("The answer to the ultimate question of life, the universe and everything is 42");
    }
}
