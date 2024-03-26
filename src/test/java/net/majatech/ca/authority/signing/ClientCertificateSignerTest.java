package net.majatech.ca.authority.signing;

import net.majatech.ca.TestUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ClientCertificateSignerTest {

    @Autowired
    private TestUtility testUtility;

    @Autowired
    private ClientCertificateSigner clientCertificateSigner;

    @Test
    public void testCorrectKeyUsageExtensionsAreUsed() {
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();
        X509Certificate certificate = clientCertificateSigner.sign(csr).getX509Certificate();

        /*
        According to the JavaDocs for X509Certificate.getKeyUsage()...

        KeyUsage ::= BIT STRING {
          digitalSignature        (0),
          nonRepudiation          (1),
          keyEncipherment         (2),
          dataEncipherment        (3),
          keyAgreement            (4),
          keyCertSign             (5),
          cRLSign                 (6),
          encipherOnly            (7),
          decipherOnly            (8)
         }

         Our certificate should contain digitalSignature, nonRepudiation and keyEncipherment
         */
        assertThat(certificate.getKeyUsage()[0]).isTrue();
        assertThat(certificate.getKeyUsage()[1]).isTrue();
        assertThat(certificate.getKeyUsage()[2]).isTrue();

        // The rest should not be used
        assertThat(certificate.getKeyUsage()[3]).isFalse();
        assertThat(certificate.getKeyUsage()[4]).isFalse();
        assertThat(certificate.getKeyUsage()[5]).isFalse();
        assertThat(certificate.getKeyUsage()[6]).isFalse();
        assertThat(certificate.getKeyUsage()[7]).isFalse();
        assertThat(certificate.getKeyUsage()[8]).isFalse();
    }

    @Test
    public void testCorrectExtendedKeyUsageExtensionsAreUsed() throws Exception {
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();
        X509Certificate certificate = clientCertificateSigner.sign(csr).getX509Certificate();

        // KeyPurpose of ClientAuth has an ID of 1.3.6.1.5.5.7.3.2
        assertThat(certificate.getExtendedKeyUsage().size()).isEqualTo(1);
        assertThat(certificate.getExtendedKeyUsage().get(0)).isEqualTo("1.3.6.1.5.5.7.3.2");
    }
}
