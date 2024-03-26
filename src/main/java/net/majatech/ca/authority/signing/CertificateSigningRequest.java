package net.majatech.ca.authority.signing;

import net.majatech.ca.authority.certificate.DistinguishedName;
import net.majatech.ca.exceptions.CaException;
import net.majatech.ca.utils.KeyUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Wrapper class to hold the CSR as well as the data corresponding to it, such as the KeyPair and Subject DN
 */
public class CertificateSigningRequest {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private final KeyPair keyPair;
    private final PKCS10CertificationRequest csr;
    private final DistinguishedName distinguishedName;

    private CertificateSigningRequest(KeyPair keyPair, PKCS10CertificationRequest csr, DistinguishedName dn) {
        this.keyPair = keyPair;
        this.csr = csr;
        this.distinguishedName = dn;
    }

    /**
     * Static builder which will generate a new KeyPair and create a PKCS10 compliant CSR with the provided DN
     * @param dn The Subject Distinguished Name to use in the certificate
     * @return The CSR wrapper containing all of the related data
     */
    public static CertificateSigningRequest using(DistinguishedName dn) {
        final KeyPair keyPair = KeyUtils.generateKeyPair();

        try {
            ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.getPrivate());

            PKCS10CertificationRequestBuilder requestBuilder =
                    new JcaPKCS10CertificationRequestBuilder(dn.toX500Name(), keyPair.getPublic());
            PKCS10CertificationRequest csr = requestBuilder.build(signer);

            return new CertificateSigningRequest(keyPair, csr, dn);
        } catch (OperatorCreationException e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public DistinguishedName getDistinguishedName() {
        return distinguishedName;
    }

    public PKCS10CertificationRequest getCsr() {
        return csr;
    }
}
