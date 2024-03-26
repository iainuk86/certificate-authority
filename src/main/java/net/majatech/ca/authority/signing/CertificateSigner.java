package net.majatech.ca.authority.signing;

import net.majatech.ca.authority.certificate.CertificateHolder;
import net.majatech.ca.exceptions.CaException;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Abstract class that makes use of the Template Design Pattern
 * <br><br>
 * As the process for signing different certificates is more or less the same - they only really differ by the
 * extensions that are used - it seemed natural to implement this logic using the Template pattern
 * <br><br>
 * The bulk of the logic - the Template - is implemented in this class, and Signers for different types of certificate
 * can extend this class to add the extensions that are specific to that certificate type
 */
public abstract class CertificateSigner {
    private static final int SERIAL_NUMBER_LENGTH = 128;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * Signs the provided CSR with the Root CA Certificate
     * @param csr The CSR to sign
     * @return A CertificateHolder containing the signed certificate and its corresponding data
     */
    public CertificateHolder sign(CertificateSigningRequest csr) {
        try {
            IssuerInfo issuerInfo = fetchIssuerInfo();
            BigInteger serialNumber = new BigInteger(SERIAL_NUMBER_LENGTH, new SecureRandom());

            PublicKey subjectPublicKey = csr.getPublicKey();
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(subjectPublicKey.getEncoded());

            // Set certificate validity to 1 year
            Instant notBefore = Instant.now();
            Instant notAfter = notBefore.plus(365, ChronoUnit.DAYS);

            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            X509v3CertificateBuilder certBuilder =
                    new X509v3CertificateBuilder(
                            issuerInfo.getDistinguishedName(),
                            serialNumber,
                            Date.from(notBefore),
                            Date.from(notAfter),
                            csr.getDistinguishedName().toX500Name(),
                            subPubKeyInfo)
                            .addExtension(Extension.keyUsage, false, getKeyUsage())
                            .addExtension(Extension.extendedKeyUsage, true, getExtendedKeyUsage())
                            .addExtension(Extension.authorityKeyIdentifier, false,
                                    extUtils.createAuthorityKeyIdentifier(issuerInfo.keyPair().getPublic()))
                            .addExtension(Extension.subjectKeyIdentifier, false,
                                    extUtils.createSubjectKeyIdentifier(subjectPublicKey));

            ContentSigner signer =
                    new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(issuerInfo.keyPair().getPrivate());
            X509CertificateHolder holder = certBuilder.build(signer);
            X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);

            // Verify the certificate was signed correctly
            cert.checkValidity();
            cert.verify(issuerInfo.keyPair().getPublic());

            return CertificateHolder.with(cert, csr.getKeyPair(), issuerInfo);
        } catch (Exception e) {
            throw new CaException(e.getMessage(), e);
        }
    }

    /**
     * The IssuerInfo in the case of this demonstration project is just all data relating to the Root CA certificate.
     * <br><br>
     * This method will fetch the Root CA certificate as well as its corresponding private key for signing purposes.
     * @return The IssuerInfo for the Root CA certificate
     */
    private IssuerInfo fetchIssuerInfo() {
        X509Certificate caCert;
        PublicKey rootCaPubKey;
        PrivateKey rootCaPrivKey = getRootCaPrivateKey();

        try (InputStream is = new ClassPathResource("/ca/rootca.pem").getInputStream()) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");

            caCert = (X509Certificate) factory.generateCertificate(is);
            rootCaPubKey = caCert.getPublicKey();
        } catch (Exception e) {
            throw new CaException("Unexpected error occurred during CA Certificate retrieval");
        }

        return IssuerInfo.from(new KeyPair(rootCaPubKey, rootCaPrivKey), caCert);
    }

    private PrivateKey getRootCaPrivateKey() {
        try (InputStream is = new ClassPathResource("/ca/private.der").getInputStream()) {
            KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(is.readAllBytes()));
        } catch (Exception e) {
            throw new CaException("Unexpected error occurred while reading Root CA Private Key");
        }
    }

    // Abstract methods to be implemented by any class extending this one
    public abstract KeyUsage getKeyUsage();
    public abstract ExtendedKeyUsage getExtendedKeyUsage();
}
