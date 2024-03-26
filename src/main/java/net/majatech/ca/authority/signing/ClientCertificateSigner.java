package net.majatech.ca.authority.signing;

import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.springframework.stereotype.Component;

/**
 * Extends the CertificateSigner abstract class to add extensions that are specific to Client Certificates
 */
@Component
public class ClientCertificateSigner extends CertificateSigner {

    @Override
    public KeyUsage getKeyUsage() {
       return new KeyUsage(KeyUsage.nonRepudiation | KeyUsage.keyEncipherment | KeyUsage.digitalSignature);
    }

    @Override
    public ExtendedKeyUsage getExtendedKeyUsage() {
        return new ExtendedKeyUsage(new KeyPurposeId[] {KeyPurposeId.id_kp_clientAuth});
    }
}
