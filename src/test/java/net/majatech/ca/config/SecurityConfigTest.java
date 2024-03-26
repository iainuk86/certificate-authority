package net.majatech.ca.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SecurityConfigTest {

    @Test
    public void testBouncyCastleProviderIsAdded() {
        assertThat(Security.getProvider("BC")).isNotNull();
    }
}
