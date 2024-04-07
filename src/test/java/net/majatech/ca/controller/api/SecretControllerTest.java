package net.majatech.ca.controller.api;

import net.majatech.ca.TestUtility;
import net.majatech.ca.authority.certificate.DistinguishedName;
import net.majatech.ca.services.SecretService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"ca.secret-url=https://ca.majatech.net:6789/api/secrets"})
public class SecretControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtility testUtility;

    @SpyBean
    private SecretService secretService;

    @Test
    @WithMockUser
    public void testFetchSecrets() throws Exception {
        // Generate a new KeyStore
        DistinguishedName subjectDn = DistinguishedName.newBuilder()
                .setCommonName("test-cn")
                .setLocality("Sydney")
                .setState("NSW")
                .setCountry("AU")
                .setOrganization("MajaTech")
                .setOrganizationalUnit("CA")
                .build();

        String savedKeyStoreId = mockMvc.perform(post("/api/keystore/csr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("commonName", subjectDn.getCommonName())
                        .param("locality", subjectDn.getLocality())
                        .param("state", subjectDn.getState())
                        .param("country", subjectDn.getCountry())
                        .param("organization", subjectDn.getOrganization())
                        .param("organizationalUnit", subjectDn.getOrganizationalUnit())
                        .param("keyStorePass", "123456")
                        .param("keyStoreAlias", "alias")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        // The actual SSL handshake and message sending can not be simulated using MockMVC. Note to research more
        Mockito.doReturn("Secret Placeholder").when(secretService).sendRequestWithSslContext(any());

        // Attempt to fetch secrets with the generated KeyStore
        String secret = mockMvc.perform(get("/api/secret/" + savedKeyStoreId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertThat(secret).isEqualTo("Secret Placeholder");

        // Cleanup S3 Bucket
        testUtility.cleanUpKeyStoreFromS3Bucket(UUID.fromString(savedKeyStoreId));
    }
}
