package net.majatech.ca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ca")
public class CaSettings {

    private String secretUrl;

    public String getSecretUrl() {
        return secretUrl;
    }

    public void setSecretUrl(String secretUrl) {
        this.secretUrl = secretUrl;
    }
}
