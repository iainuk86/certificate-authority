package net.majatech.ca.services;

import jakarta.transaction.Transactional;
import net.majatech.ca.authority.*;
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

    @Transactional
    public void saveUploadedKeyStore(MultipartFile file, String pass, String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(file.getInputStream(), pass.toCharArray());

            if (!(keyStore.getCertificate(alias) instanceof X509Certificate x509Certificate)) {
                throw new CaException("No X509Certificate exists with that alias");
            }
            x509Certificate.checkValidity();

            KeyStoreInfo keyStoreInfo = KeyStoreInfo.from(x509Certificate, pass, alias);
            saveKeyStore(keyStore, keyStoreInfo);
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CaException("Certificate is not valid. Please check the expiry", e);
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    public void saveKeyStore(KeyStore keyStore, KeyStoreInfo keyStoreInfo) {
        keyStoreInfoRepository.save(keyStoreInfo);
        saveKeyStoreToFileSystem(keyStore, keyStoreInfo);
    }

    public CertificateSigningRequest createCsr(CsrForm csrForm) {
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

    @Transactional
    public void generateKeyStoreFromCsr(CsrForm csrForm) {
        CertificateSigningRequest csr = createCsr(csrForm);
        CertificateHolder certHolder = clientCertificateSigner.sign(csr);

        // Persist KeyStore metadata to database
        KeyStoreInfo keyStoreInfo =
                saveKeyStoreInfo(
                        KeyStoreInfo.from(
                                certHolder.getX509Certificate(),
                                csrForm.getKeyStorePass(),
                                csrForm.getKeyStoreAlias()
                        )
                );

        // Save KeyStore to file system for easy retrieval and usage
        saveKeyStoreToFileSystem(
                certHolder.generateKeyStore(csrForm.getKeyStorePass(), csrForm.getKeyStoreAlias()), keyStoreInfo);
    }

    public KeyStoreInfo getKeyStoreInfo(UUID keyStoreId) {
        return keyStoreInfoRepository.findById(keyStoreId).get();
    }

    public byte[] getKeyStore(UUID keyStoreId) {
        KeyStoreInfo keyStoreInfo = getKeyStoreInfo(keyStoreId);

        try (InputStream is = new FileInputStream(getKeyStorePath(keyStoreId))) {
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

    @Transactional
    public void deleteKeyStore(UUID keyStoreId) {
        keyStoreInfoRepository.deleteById(keyStoreId);

        try {
            Files.delete(Paths.get(getKeyStorePath(keyStoreId)));
        } catch (IOException e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    private void saveKeyStoreToFileSystem(KeyStore keyStore, KeyStoreInfo keyStoreInfo) {
        try (OutputStream outputStream = new FileOutputStream(getKeyStorePath(keyStoreInfo.keyStoreId))) {
            keyStore.store(outputStream, keyStoreInfo.pass.toCharArray());
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    @Transactional
    public KeyStoreInfo saveKeyStoreInfo(KeyStoreInfo keyStoreInfo) {
        return keyStoreInfoRepository.save(keyStoreInfo);
    }

    public List<KeyStoreInfo> getKeyStores() {
        return keyStoreInfoRepository.findAll();
    }

    private static String getKeyStorePath(UUID keyStoreId) {
        return KEYSTORE_ROOT_DIRECTORY + keyStoreId + ".p12";
    }
}
