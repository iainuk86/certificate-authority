package net.majatech.ca.services;

import net.majatech.ca.config.CaSettings;
import net.majatech.ca.data.entity.KeyStoreInfo;
import net.majatech.ca.exceptions.CaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Collections;
import java.util.UUID;

/**
 * Service class to handle all communication with the S3 Bucket where the Client Certificates / KeyStores are saved
 */
@Service
public class S3Service {

    private final CaSettings caSettings;
    private final S3Client s3Client;

    @Autowired
    public S3Service(CaSettings caSettings) {
        this.caSettings = caSettings;
        this.s3Client = buildS3Client();
    }

    /**
     * Upload the provided KeyStore to the S3 Bucket
     * @param keyStore The KeyStore to save
     * @param keyStoreInfo The KeyStore metadata
     */
    public void saveKeyStore(KeyStore keyStore, KeyStoreInfo keyStoreInfo) {
        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(caSettings.getAws().getS3Bucket())
                .key(keyStoreInfo.getKeyStoreId().toString())
                .build();

        s3Client.putObject(putOb, RequestBody.fromBytes(getKeyStoreBytes(keyStore, keyStoreInfo.getPass())));
    }

    /**
     * Convenience method to fetch a KeyStore using the KeyStoreInfo entity
     * @param keyStoreInfo The metadata of the KeyStore to retrieve
     * @return The KeyStore
     */
    public KeyStore fetchKeyStore(KeyStoreInfo keyStoreInfo) {
        return fetchKeyStore(keyStoreInfo.keyStoreId, keyStoreInfo.getPass());
    }

    /**
     * Retrieves a KeyStore from the S3 Bucket
     * @param keyStoreId The ID of the KeyStore to retrieve
     * @param pass The password of the KeyStore
     * @return The KeyStore
     */
    public KeyStore fetchKeyStore(UUID keyStoreId, String pass) {
        try (InputStream is = new ByteArrayInputStream(fetchKeyStoreAsBytes(keyStoreId))) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, pass.toCharArray());

            return keyStore;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Fetch the requested KeyStore as a byte array
     * @param keyStoreId The ID of the KeyStore to retrieve
     * @return The KeyStore represented as a byte array
     */
    public byte[] fetchKeyStoreAsBytes(UUID keyStoreId) {
        GetObjectRequest getOb = GetObjectRequest
                .builder()
                .key(keyStoreId.toString())
                .bucket(caSettings.getAws().getS3Bucket())
                .build();

        return s3Client.getObjectAsBytes(getOb).asByteArray();
    }

    /**
     * Delete the designated KeyStore from the S3 Bucket
     * @param keyStoreId The ID of the KeyStore to delete
     */
    public void deleteKeyStore(UUID keyStoreId) {
        ObjectIdentifier objectId = ObjectIdentifier.builder()
                .key(keyStoreId.toString())
                .build();

        Delete del = Delete.builder()
                .objects(Collections.singleton(objectId))
                .build();

        try {
            DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder()
                    .bucket(caSettings.getAws().getS3Bucket())
                    .delete(del)
                    .build();

            s3Client.deleteObjects(multiObjectDeleteRequest);
        } catch (S3Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * Convert the provided KeyStore object to a byte array representation
     * @param keyStore The KeyStore to convert
     * @param pass The KeyStore password
     * @return The KeyStore represented as a byte array
     */
    private byte[] getKeyStoreBytes(KeyStore keyStore, String pass) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            keyStore.store(baos, pass.toCharArray());

            byte[] bytes = baos.toByteArray();
            baos.close();

            return bytes;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    private S3Client buildS3Client() {
        return S3Client.builder()
                .region(Region.of(caSettings.getAws().getRegion()))
                .build();
    }
}
