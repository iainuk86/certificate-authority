package net.majatech.ca.authority.certificate;

import net.majatech.ca.TestUtility;
import net.majatech.ca.authority.signing.CertificateSigningRequest;
import net.majatech.ca.authority.signing.ClientCertificateSigner;
import net.majatech.ca.exceptions.CaException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@SpringBootTest
public class CertificateHolderTest {

    @Autowired
    private TestUtility testUtility;

    @Autowired
    private ClientCertificateSigner clientCertificateSigner;

    @Test
    public void testSignedCertificateDnMatchesCsr() {
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();
        CertificateHolder certificateHolder = clientCertificateSigner.sign(csr);

        // Assert the keys are the same
        assertThat(csr.getKeyPair().getPublic()).isEqualTo(certificateHolder.getKeyPair().getPublic());
        assertThat(csr.getKeyPair().getPrivate()).isEqualTo(certificateHolder.getKeyPair().getPrivate());

        X500Name x500Name =
                X500Name.getInstance(certificateHolder.getX509Certificate().getSubjectX500Principal().getEncoded());

        assertDistinguishedNamesAreEqual(x500Name, csr.getDistinguishedName());
    }

    @Test
    public void testGenerateKeyStore() throws Exception {
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();
        CertificateHolder certificateHolder = clientCertificateSigner.sign(csr);
        KeyStore generatedKeyStore = certificateHolder.generateKeyStore("testing", "test-alias");

        assertThat(generatedKeyStore.getType()).isEqualTo("PKCS12");
        assertThat(generatedKeyStore.containsAlias("test-alias")).isTrue();
        assertThat(generatedKeyStore.isKeyEntry("test-alias")).isTrue();

        Certificate certificate = generatedKeyStore.getCertificate("test-alias");
        assertThat(certificate).isInstanceOf(X509Certificate.class);
        assertThat(certificate.getPublicKey()).isEqualTo(csr.getPublicKey());

        X509Certificate x509 = (X509Certificate) certificate;
        x509.checkValidity();
        x509.verify(certificateHolder.getIssuerInfo().keyPair().getPublic());

        assertDistinguishedNamesAreEqual(
                X500Name.getInstance(x509.getSubjectX500Principal().getEncoded()), csr.getDistinguishedName());
    }

    @Test
    public void testGenerateKeyStoreWithNullPasswordThrowsException() {
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();
        CertificateHolder certificateHolder = clientCertificateSigner.sign(csr);

        try {
            certificateHolder.generateKeyStore(null, "test-alias");
            fail("No exception was thrown");
        } catch (CaException e) {
            assertThat(e.getMessage()).isNotNull().isNotEmpty();
        } catch (Exception e) {
            fail("CaException was not thrown");
        }
    }

    private void assertDistinguishedNamesAreEqual(X500Name x500Name, DistinguishedName dn) {
        assertThat(dnFieldValue(x500Name, BCStyle.CN)).isEqualTo(dn.getCommonName());
        assertThat(dnFieldValue(x500Name, BCStyle.L)).isEqualTo(dn.getLocality());
        assertThat(dnFieldValue(x500Name, BCStyle.ST)).isEqualTo(dn.getState());
        assertThat(dnFieldValue(x500Name, BCStyle.C)).isEqualTo(dn.getCountry());
        assertThat(dnFieldValue(x500Name, BCStyle.O)).isEqualTo(dn.getOrganization());
        assertThat(dnFieldValue(x500Name, BCStyle.OU)).isEqualTo(dn.getOrganizationalUnit());
    }

    private String dnFieldValue(X500Name x500Name, ASN1ObjectIdentifier oid) {
        return IETFUtils.valueToString(x500Name.getRDNs(oid)[0].getFirst().getValue());
    }
}
