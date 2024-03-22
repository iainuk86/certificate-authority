package net.majatech.ca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "net.majatech.ca.data.repo")
public class CaApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaApplication.class, args);
    }
}
