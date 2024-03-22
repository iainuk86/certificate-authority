package net.majatech.ca.authority.signing;

import net.majatech.ca.authority.DistinguishedName;
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

public class CertificateSigningRequest {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final PKCS10CertificationRequest csr;
    private final DistinguishedName distinguishedName;

    private CertificateSigningRequest(KeyPair keyPair, PKCS10CertificationRequest csr, DistinguishedName dn) {
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        this.csr = csr;
        this.distinguishedName = dn;
    }

    public static CertificateSigningRequest using(DistinguishedName dn) {
        final KeyPair keyPair = KeyUtils.generateKeyPair();

        try {
            ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.getPrivate());

            PKCS10CertificationRequestBuilder requestBuilder =
                    new JcaPKCS10CertificationRequestBuilder(dn.toX500Name(), keyPair.getPublic());
            PKCS10CertificationRequest csr = requestBuilder.build(signer);

            return new CertificateSigningRequest(keyPair, csr, dn);
        } catch (final OperatorCreationException e) {
            throw new CaException(e.getMessage());
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public DistinguishedName getDistinguishedName() {
        return distinguishedName;
    }

    public PKCS10CertificationRequest getCsr() {
        return csr;
    }
}
