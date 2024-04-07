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

import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

@Service
public class KeyStoreService {

    private final S3Service s3Service;
    private final KeyStoreInfoRepository keyStoreInfoRepository;
    private final ClientCertificateSigner clientCertificateSigner;

    @Autowired
    public KeyStoreService(S3Service s3Service, KeyStoreInfoRepository keyStoreInfoRepository,
                           ClientCertificateSigner clientCertificateSigner) {
        this.s3Service = s3Service;
        this.keyStoreInfoRepository = keyStoreInfoRepository;
        this.clientCertificateSigner = clientCertificateSigner;
    }

    /**
     * Use the CSR values received from the UI to generate, sign and save the resulting KeyStore
     * <br><br>
     * Saves the KeyStore metadata into the database and uploads the KeyStore itself to the S3 Bucket
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

        // Upload KeyStore to S3 bucket
        s3Service.saveKeyStore(
                certHolder.generateKeyStore(csrForm.getKeyStorePass(), csrForm.getKeyStoreAlias()), keyStoreInfo);

        return keyStoreInfo.getKeyStoreId().toString();
    }

    /**
     * Save the uploaded KeyStore file to the application
     * <br><br>
     * Will first verify the provided file, password and entry alias are valid. Then creates the metadata domain class
     * using the KeyStore information. This is then saved to the database and the KeyStore is uploaded to the S3 Bucket
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

            return keyStoreInfo.getKeyStoreId().toString();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CaException("Certificate is not valid. Please check the expiry", e);
        } catch (Exception e) {
            throw new CaException("Please upload a valid PKCS12 Certificate", e);
        }
    }

    /**
     * Retrieves the desired KeyStore and converts it to a byte array to allow for it to be downloaded from the UI
     * @param keyStoreId The ID of the KeyStore to be retrieved
     * @return The KeyStore represented as a byte array
     */
    public byte[] getKeyStoreBytes(UUID keyStoreId) {
        keyStoreInfoRepository.findById(keyStoreId).orElseThrow(() -> new CaException("KeyStore not found"));

        return s3Service.fetchKeyStoreAsBytes(keyStoreId);
    }

    /**
     * Deletes the KeyStore metadata from the database as well as the corresponding KeyStore from the S3 Bucket
     * @param keyStoreId The ID of the KeyStore to delete
     */
    @Transactional
    public void deleteKeyStore(UUID keyStoreId) {
        keyStoreInfoRepository.deleteById(keyStoreId);
        s3Service.deleteKeyStore(keyStoreId);
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
     * Saves the provided KeyStore metadata to the database and the KeyStore to the S3 Bucket
     * @param keyStore The KeyStore to save
     * @param keyStoreInfo The metadata corresponding to the KeyStore
     */
    private void saveKeyStore(KeyStore keyStore, KeyStoreInfo keyStoreInfo) {
        keyStoreInfoRepository.save(keyStoreInfo);
        s3Service.saveKeyStore(keyStore, keyStoreInfo);
    }
}
