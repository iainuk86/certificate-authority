package net.majatech.ca.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {"ca.secret-url=https://super-secret.shh"})
public class CaSettingsTest {

    @Autowired
    private CaSettings caSettings;

    @Test
    public void testSettingsAreLoaded() {
        assertThat(caSettings.getSecretUrl()).isEqualTo("https://super-secret.shh");

        caSettings.setSecretUrl("https://updated-secret.shh");
        assertThat(caSettings.getSecretUrl()).isEqualTo("https://updated-secret.shh");
    }
}
