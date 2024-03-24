package net.majatech.ca.services;

import net.majatech.ca.data.entity.KeyStoreInfo;
import net.majatech.ca.data.repo.KeyStoreInfoRepository;
import net.majatech.ca.exceptions.CaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.UUID;

@Service
public class SecretService {

    private static final String SECRET_URL = "https://ca.majatech.net:6789/api/secrets";

    private final KeyStoreInfoRepository keyStoreInfoRepository;

    @Autowired
    public SecretService(KeyStoreInfoRepository keyStoreInfoRepository) {
        this.keyStoreInfoRepository = keyStoreInfoRepository;
    }

    public String fetchSecrets(UUID keyStoreId) {
        // Fetch the KeyStore metadata
        KeyStoreInfo keyStoreInfo = keyStoreInfoRepository.findById(keyStoreId).get();

        // Load the corresponding KeyStore
        KeyStore keyStore = loadKeyStore(keyStoreInfo);

        // Create the manager factories
        KeyManagerFactory kmf = loadKeyManagerFactory(keyStore, keyStoreInfo.pass);
        TrustManagerFactory tmf = loadTrustManagerFactory();

        // Create the SSL Context
        SSLContext sslContext = loadSslContext(kmf, tmf);

        // Create HTTP Client with the SSL Context
        try (HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .sslContext(sslContext)
                .build()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SECRET_URL))
                    .GET()
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    private KeyStore loadKeyStore(KeyStoreInfo keyStoreInfo) {
        try (InputStream is = new FileInputStream(KeyStoreService.getKeyStorePath(keyStoreInfo.keyStoreId))) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, keyStoreInfo.getPass().toCharArray());

            return keyStore;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    private KeyManagerFactory loadKeyManagerFactory(KeyStore keyStore, String pass) {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, pass.toCharArray());

            return kmf;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    private TrustManagerFactory loadTrustManagerFactory() {
        try (InputStream is = new ClassPathResource("/ca/ca.p12").getInputStream()) {
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(is, "123456".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            return tmf;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    private SSLContext loadSslContext(KeyManagerFactory kmf, TrustManagerFactory tmf) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            return sslContext;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }
}
