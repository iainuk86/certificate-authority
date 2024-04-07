package net.majatech.ca.controller.api;

import net.majatech.ca.TestUtility;
import net.majatech.ca.authority.certificate.CertificateHolder;
import net.majatech.ca.authority.certificate.DistinguishedName;
import net.majatech.ca.authority.signing.CertificateSigningRequest;
import net.majatech.ca.authority.signing.ClientCertificateSigner;
import net.majatech.ca.data.entity.KeyStoreInfo;
import net.majatech.ca.data.repo.KeyStoreInfoRepository;
import org.bouncycastle.asn1.x500.X500Name;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class KeyStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeyStoreInfoRepository keyStoreInfoRepository;

    @Autowired
    private ClientCertificateSigner clientCertificateSigner;

    @Autowired
    private TestUtility testUtility;

    @Test
    @WithMockUser
    public void testGenerateAndSaveKeyStoreFromCsr() throws Exception {
        DistinguishedName subjectDn = DistinguishedName.newBuilder()
                .setCommonName("test-cn")
                .setLocality("Sydney")
                .setState("NSW")
                .setCountry("AU")
                .setOrganization("MajaTech")
                .setOrganizationalUnit("CA")
                .build();

        // Generate and save a new KeyStore by calling the controller CSR endpoint
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

        // Assert the KeyStore metadata was saved to the database
        KeyStoreInfo savedInfo = keyStoreInfoRepository.findById(UUID.fromString(savedKeyStoreId)).get();
        assertThat(savedInfo.getPass()).isEqualTo("123456");
        assertThat(savedInfo.getAlias()).isEqualTo("alias");

        X500Name x500Name = new X500Name(savedInfo.getSubjectDn());

        testUtility.assertDistinguishedNamesAreEqual(x500Name, subjectDn);

        // Assert the KeyStore itself was saved to the S3 Bucket
        KeyStore savedKeyStore = testUtility.fetchSavedKeyStore(UUID.fromString(savedKeyStoreId), savedInfo.getPass());
        assertThat(savedKeyStore.containsAlias("alias")).isTrue();
        assertThat(savedKeyStore.isKeyEntry("alias")).isTrue();

        Certificate certificate = savedKeyStore.getCertificate("alias");
        X509Certificate x509 = (X509Certificate) certificate;
        x509.checkValidity();

        testUtility.assertDistinguishedNamesAreEqual(
                X500Name.getInstance(x509.getSubjectX500Principal().getEncoded()), subjectDn);

        // Cleanup S3 Bucket
        testUtility.cleanUpKeyStoreFromS3Bucket(UUID.fromString(savedKeyStoreId));
    }

    @Test
    @WithMockUser
    public void testCsrWithoutPasswordIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/keystore/csr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("commonName", "test-cn")
                        .param("locality", "Sydney")
                        .param("state", "NSW")
                        .param("country", "AU")
                        .param("organization", "MajaTech")
                        .param("organizationalUnit", "CA")
                        .param("keyStoreAlias", "alias")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void testCsrWithoutAliasIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/keystore/csr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("commonName", "test-cn")
                        .param("locality", "Sydney")
                        .param("state", "NSW")
                        .param("country", "AU")
                        .param("organization", "MajaTech")
                        .param("organizationalUnit", "CA")
                        .param("keyStorePass", "123456")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnauthorisedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(post("/api/keystore/csr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("commonName", "test-cn")
                        .param("locality", "Sydney")
                        .param("state", "NSW")
                        .param("country", "AU")
                        .param("organization", "MajaTech")
                        .param("organizationalUnit", "CA")
                        .param("keyStorePass", "123456")
                        .param("keyStoreAlias", "alias")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    public void testUploadingKeyStore() throws Exception{
        // First create a file that we will 'upload'
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();
        CertificateHolder certificateHolder = clientCertificateSigner.sign(csr);
        KeyStore keyStore = certificateHolder.generateKeyStore("123456", "alias");

        // Convert KeyStore to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        keyStore.store(baos, "123456".toCharArray());

        byte[] keyStoreBytes = baos.toByteArray();
        baos.close();

        // Simulate uploading the file
        String savedKeyStoreId = mockMvc.perform(multipart("/api/keystore/upload")
                        .file("ks", keyStoreBytes)
                        .param("pass", "123456")
                        .param("alias", "alias")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn().getResponse().getContentAsString();

        // Assert the KeyStore metadata was saved to the database
        KeyStoreInfo savedInfo = keyStoreInfoRepository.findById(UUID.fromString(savedKeyStoreId)).get();
        assertThat(savedInfo.getPass()).isEqualTo("123456");
        assertThat(savedInfo.getAlias()).isEqualTo("alias");

        X500Name x500Name = new X500Name(savedInfo.getSubjectDn());

        testUtility.assertDistinguishedNamesAreEqual(x500Name, csr.getDistinguishedName());

        // Assert the KeyStore itself was saved to the S3 Bucket
        KeyStore savedKeyStore = testUtility.fetchSavedKeyStore(UUID.fromString(savedKeyStoreId), savedInfo.getPass());
        assertThat(savedKeyStore.containsAlias("alias")).isTrue();
        assertThat(savedKeyStore.isKeyEntry("alias")).isTrue();

        Certificate certificate = savedKeyStore.getCertificate("alias");
        X509Certificate x509 = (X509Certificate) certificate;
        x509.checkValidity();

        testUtility.assertDistinguishedNamesAreEqual(
                X500Name.getInstance(x509.getSubjectX500Principal().getEncoded()), csr.getDistinguishedName());

        // Cleanup S3 Bucket
        testUtility.cleanUpKeyStoreFromS3Bucket(UUID.fromString(savedKeyStoreId));
    }

    @Test
    @WithMockUser
    public void testGetKeyStoreAsByteArrayForFileDownload() throws Exception{
        // First create a KeyStore and keep a record of the subject DN
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

        // Now simulate downloading the KeyStore
        byte[] keyStoreBytes = mockMvc.perform(get("/api/keystore/download/" + savedKeyStoreId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsByteArray();

        KeyStore downloadedKeyStore = KeyStore.getInstance("PKCS12");
        downloadedKeyStore.load(new ByteArrayInputStream(keyStoreBytes), "123456".toCharArray());

        assertThat(downloadedKeyStore.containsAlias("alias")).isTrue();
        assertThat(downloadedKeyStore.isKeyEntry("alias")).isTrue();

        Certificate certificate = downloadedKeyStore.getCertificate("alias");
        X509Certificate x509 = (X509Certificate) certificate;
        x509.checkValidity();

        testUtility.assertDistinguishedNamesAreEqual(
                X500Name.getInstance(x509.getSubjectX500Principal().getEncoded()), subjectDn);

        // Cleanup S3 Bucket
        testUtility.cleanUpKeyStoreFromS3Bucket(UUID.fromString(savedKeyStoreId));
    }

    @Test
    @WithMockUser
    public void testDeleteKeyStore() throws Exception{
        // First create a KeyStore
        String savedKeyStoreId = mockMvc.perform(post("/api/keystore/csr")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("commonName", "test-cn")
                        .param("locality", "Sydney")
                        .param("state", "NSW")
                        .param("country", "AU")
                        .param("organization", "MajaTech")
                        .param("organizationalUnit", "CA")
                        .param("keyStorePass", "123456")
                        .param("keyStoreAlias", "alias")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        // Assert KeyStore is saved
        assertThat(keyStoreInfoRepository.findById(UUID.fromString(savedKeyStoreId))).isPresent();

        // Now delete the keystore
        mockMvc.perform(post("/api/keystore/delete/" + savedKeyStoreId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print());

        // Assert KeyStore has been deleted
        assertThat(keyStoreInfoRepository.findById(UUID.fromString(savedKeyStoreId))).isNotPresent();
    }
}
