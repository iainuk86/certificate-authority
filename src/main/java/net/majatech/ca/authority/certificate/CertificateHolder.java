package net.majatech.ca.authority.certificate;

import net.majatech.ca.authority.signing.IssuerInfo;
import net.majatech.ca.exceptions.CaException;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Class to hold all data relating to the contained certificate
 * <br><br>
 * Includes the certificate itself, the matching KeyPair as well as information about the certificate Issuer
 */
public class CertificateHolder {
    private final X509Certificate x509Certificate;
    private final KeyPair keyPair;
    private final IssuerInfo issuerInfo;

    private CertificateHolder(X509Certificate x509Certificate, KeyPair keyPair, IssuerInfo issuerInfo) {
        this.x509Certificate = x509Certificate;
        this.keyPair = keyPair;
        this.issuerInfo = issuerInfo;
    }

    public static CertificateHolder with(X509Certificate x509Certificate, KeyPair keyPair, IssuerInfo issuerInfo) {
        return new CertificateHolder(x509Certificate, keyPair, issuerInfo);
    }

    /**
     * Generate a PKCS12 KeyStore using the data contained within an instance of this class
     * @param pass The password for the new KeyStore
     * @param alias The alias of the key entry within the store
     * @return The generated PKCS12 KeyStore
     */
    public KeyStore generateKeyStore(String pass, String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, "integrity-check".toCharArray());
            keyStore.setKeyEntry(alias, keyPair.getPrivate(), pass.toCharArray(),
                    new X509Certificate[] {x509Certificate, issuerInfo.rootCa()});

            return keyStore;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public IssuerInfo getIssuerInfo() {
        return issuerInfo;
    }
}
