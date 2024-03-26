package net.majatech.ca.services;

import jakarta.transaction.Transactional;
import net.majatech.ca.authority.certificate.CertificateHolder;
import net.majatech.ca.authority.certificate.DistinguishedName;
import net.majatech.ca.authority.signing.CertificateSigningRequest;
import net.majatech.ca.authority.signing.ClientCertificateSigner;
import net.majatech.ca.controller.api.model.CsrForm;
import net.majatech.ca.data.entity.KeyStoreInfo;
import net.majatech.ca.data.repo.KeyStoreInfoRepository;
import net.majatech.ca.exceptions.CaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

@Service
public class KeyStoreService {

    private static final String KEYSTORE_ROOT_DIRECTORY = "C:\\Users\\iainu\\IdeaProjects\\ca\\src\\main\\resources\\clients\\";

    private final KeyStoreInfoRepository keyStoreInfoRepository;
    private final ClientCertificateSigner clientCertificateSigner;

    @Autowired
    public KeyStoreService(KeyStoreInfoRepository keyStoreInfoRepository,
                           ClientCertificateSigner clientCertificateSigner) {
        this.keyStoreInfoRepository = keyStoreInfoRepository;
        this.clientCertificateSigner = clientCertificateSigner;
    }

    /**
     * Use the CSR values received from the UI to generate, sign and save the resulting KeyStore
     * <br><br>
     * Saves the KeyStore metadata into the database and saves the KeyStore itself to the local file system
     * @param csrForm The CSR fields received from the UI
     * @return The ID of the generated KeyStore
     */
    @Transactional
    public String generateKeyStoreFromCsr(CsrForm csrForm) {
        // Create CSR from UI values
        CertificateSigningRequest csr = createCsr(csrForm);

        // Sign the certificate
        CertificateHolder certHolder = clientCertificateSigner.sign(csr);

        // Persist KeyStore metadata to database
        KeyStoreInfo keyStoreInfo =
                keyStoreInfoRepository.save(
                        KeyStoreInfo.from(
                                certHolder.getX509Certificate(),
                                csrForm.getKeyStorePass(),
                                csrForm.getKeyStoreAlias()
                        )
                );

        // Save KeyStore to file system for easy retrieval and usage
        saveKeyStoreToFileSystem(
                certHolder.generateKeyStore(csrForm.getKeyStorePass(), csrForm.getKeyStoreAlias()), keyStoreInfo);

        return keyStoreInfo.keyStoreId.toString();
    }

    /**
     * Save the uploaded KeyStore to the system
     * <br><br>
     * Will first verify the provided file, password and entry alias are valid. Then creates the metadata domain class
     * using the KeyStore information. This is then saved to the database and the KeyStore is stored locally
     * @param file The uploaded KeyStore
     * @param pass The KeyStore password
     * @param alias The alias of the key entry/certificate to save
     * @return The ID of the uploaded KeyStore
     */
    @Transactional
    public String saveUploadedKeyStore(MultipartFile file, String pass, String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(file.getInputStream(), pass.toCharArray());

            // Verify the certificate/key exists
            if (!(keyStore.getCertificate(alias) instanceof X509Certificate x509Certificate)) {
                throw new CaException("No X509Certificate exists with that alias");
            }
            x509Certificate.checkValidity();

            // Save the KeyStore
            KeyStoreInfo keyStoreInfo = KeyStoreInfo.from(x509Certificate, pass, alias);
            saveKeyStore(keyStore, keyStoreInfo);

            return keyStoreInfo.keyStoreId.toString();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CaException("Certificate is not valid. Please check the expiry", e);
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the desired KeyStore and converts it to a byte array to allow for it to be downloaded from the UI
     * @param keyStoreId The ID of the KeyStore to be retrieved
     * @return The KeyStore represented as a byte array
     */
    public byte[] getKeyStore(UUID keyStoreId) {
        KeyStoreInfo keyStoreInfo =
                keyStoreInfoRepository.findById(keyStoreId).orElseThrow(() -> new CaException("KeyStore not found"));

        try (InputStream is = new FileInputStream(getKeyStoreResourcePath(keyStoreId))) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, keyStoreInfo.pass.toCharArray());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            keyStore.store(baos, keyStoreInfo.pass.toCharArray());

            byte[] bytes = baos.toByteArray();
            baos.close();

            return bytes;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Deletes the KeyStore from the local file system and its metadata from the database
     * @param keyStoreId The ID of the KeyStore to delete
     */
    @Transactional
    public void deleteKeyStore(UUID keyStoreId) {
        keyStoreInfoRepository.deleteById(keyStoreId);

        try {
            Files.delete(Paths.get(getKeyStoreResourcePath(keyStoreId)));
        } catch (IOException e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve metadata corresponding to all currently saved KeyStores for UI display
     * @return A list of all currently saved KeyStores
     */
    public List<KeyStoreInfo> getKeyStores() {
        return keyStoreInfoRepository.findAll();
    }

    /**
     * Creates a CSR using the data retrieved from the HTML CSR / 'Create' form
     * @param csrForm The data retrieved from the user that will be used as the Subject in the certificate
     * @return The CSR wrapper that also contains the corresponding KeyPair
     */
    private CertificateSigningRequest createCsr(CsrForm csrForm) {
        DistinguishedName subjectDn =
                DistinguishedName.newBuilder()
                        .setCommonName(csrForm.getCommonName())
                        .setLocality(csrForm.getLocality())
                        .setState(csrForm.getState())
                        .setCountry(csrForm.getCountry())
                        .setOrganization(csrForm.getOrganization())
                        .setOrganizationalUnit(csrForm.getOrganizationalUnit())
                        .build();

        return CertificateSigningRequest.using(subjectDn);
    }

    /**
     * Saves the provided KeyStore to the local file system and its metadata to the database
     * @param keyStore The KeyStore to save
     * @param keyStoreInfo The metadata corresponding to the KeyStore
     */
    private void saveKeyStore(KeyStore keyStore, KeyStoreInfo keyStoreInfo) {
        keyStoreInfoRepository.save(keyStoreInfo);
        saveKeyStoreToFileSystem(keyStore, keyStoreInfo);
    }

    /**
     * Saves the provided KeyStore to the local file system
     * @param keyStore The KeyStore to save
     * @param keyStoreInfo The metadata corresponding to the KeyStore
     */
    private void saveKeyStoreToFileSystem(KeyStore keyStore, KeyStoreInfo keyStoreInfo) {
        try (OutputStream outputStream = new FileOutputStream(getKeyStoreResourcePath(keyStoreInfo.keyStoreId))) {
            keyStore.store(outputStream, keyStoreInfo.pass.toCharArray());
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    public static String getKeyStoreResourcePath(UUID keyStoreId) {
        return KEYSTORE_ROOT_DIRECTORY + keyStoreId + ".p12";
    }
}
