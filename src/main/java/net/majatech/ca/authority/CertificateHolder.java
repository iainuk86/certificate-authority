package net.majatech.ca.authority;

import net.majatech.ca.authority.signing.IssuerInfo;
import net.majatech.ca.exceptions.CaException;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class CertificateHolder {
    private final X509Certificate x509Certificate;
    private final IssuerInfo issuerInfo;

    private CertificateHolder(X509Certificate x509Certificate, IssuerInfo issuerInfo) {
        this.x509Certificate = x509Certificate;
        this.issuerInfo = issuerInfo;
    }

    public static CertificateHolder with(X509Certificate x509Certificate, IssuerInfo issuerInfo) {
        return new CertificateHolder(x509Certificate, issuerInfo);
    }

    public KeyStore generateKeyStore(String pass, String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, "integrity-check".toCharArray());
            keyStore.setKeyEntry(alias, issuerInfo.keyPair().getPrivate(), pass.toCharArray(),
                    new X509Certificate[] {x509Certificate, issuerInfo.rootCa()});

            return keyStore;
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public IssuerInfo getIssuerInfo() {
        return issuerInfo;
    }
}
