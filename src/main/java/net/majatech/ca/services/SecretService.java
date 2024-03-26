package net.majatech.ca.services;

import net.majatech.ca.config.CaSettings;
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

    private final KeyStoreInfoRepository keyStoreInfoRepository;
    private final CaSettings caSettings;

    @Autowired
    public SecretService(KeyStoreInfoRepository keyStoreInfoRepository, CaSettings caSettings) {
        this.keyStoreInfoRepository = keyStoreInfoRepository;
        this.caSettings = caSettings;
    }

    /**
     * Main method used for attempting to fetch the protected data
     * <br><br>
     * Creates the SSLContext using the desired KeyStore and attempts connection. The JDK built-in HttpClient library
     * is used to avoid including any other dependencies, but any HttpClient can be used. Also tested with OkHttpClient
     * @param keyStoreId The ID of the KeyStore to use when creating the SSLContext
     * @return The protected data string if connection is successful. If not, the JS catches any exception or error
     * to display a generic error message for easier demonstration
     */
    public String fetchSecrets(UUID keyStoreId) {
        // Fetch the KeyStore metadata
        KeyStoreInfo keyStoreInfo =
                keyStoreInfoRepository.findById(keyStoreId)
                        .orElseThrow(() -> new IllegalArgumentException("KeyStore not found"));

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
                    .uri(URI.create(caSettings.getSecretUrl()))
                    .GET()
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Load the desired KeyStore from local storage
     * @param keyStoreInfo Metadata corresponding to a locally stored KeyStore
     * @return The KeyStore
     */
    private KeyStore loadKeyStore(KeyStoreInfo keyStoreInfo) {
        try (InputStream is = new FileInputStream(KeyStoreService.getKeyStoreResourcePath(keyStoreInfo.keyStoreId))) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, keyStoreInfo.getPass().toCharArray());

            return keyStore;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Load the KeyManagerFactory required for the SSLContext
     * @param keyStore The KeyStore to use in the SSLContext
     * @param pass The password to the KeyStore
     * @return The KeyManagerFactory
     */
    private KeyManagerFactory loadKeyManagerFactory(KeyStore keyStore, String pass) {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, pass.toCharArray());

            return kmf;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Load the TrustManagerFactory required for the SSLContext. Here, this will be the Root CA PKCS12 KeyStore
     * @return The The TrustManagerFactory
     */
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

    /**
     * Create and initialise the SSLContext using the provided manager factories
     * @param kmf The KeyManagerFactory
     * @param tmf The TrustManagerFactory
     * @return The initialised SSLContext
     */
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
