package net.majatech.ca.authority.signing;

import org.bouncycastle.asn1.x500.X500Name;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

public record IssuerInfo(KeyPair keyPair, X509Certificate rootCa) {

    public static IssuerInfo from(KeyPair keyPair, X509Certificate rootCa) {
        return new IssuerInfo(keyPair, rootCa);
    }

    public X500Name getDistinguishedName() {
        return X500Name.getInstance(rootCa.getSubjectX500Principal().getEncoded());
    }
}
