package net.majatech.ca.authority.signing;

import net.majatech.ca.TestUtility;
import net.majatech.ca.authority.certificate.DistinguishedName;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Signature;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CertificateSigningRequestTest {

    @Autowired
    private TestUtility testUtility;

    @Test
    public void testPublicAndPrivateKeysMatch() throws Exception {
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();

        byte[] challenge = new byte[10000];
        ThreadLocalRandom.current().nextBytes(challenge);

        // Sign using the private key
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(csr.getPrivateKey());
        signature.update(challenge);

        byte[] signed = signature.sign();

        // Verify signature using the public key
        signature.initVerify(csr.getPublicKey());
        signature.update(challenge);

        assertThat(signature.verify(signed)).isTrue();
    }

    @Test
    public void testWrapperClassFieldsMatchUnderlyingPKCS10() {
        CertificateSigningRequest csr = testUtility.getDefaultTestCsr();

        PKCS10CertificationRequest pkcs10Csr = csr.getCsr();
        DistinguishedName dn = csr.getDistinguishedName();

        testUtility.assertDistinguishedNamesAreEqual(pkcs10Csr.getSubject(), dn);
    }
}
