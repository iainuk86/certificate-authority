package net.majatech.ca.utils;

import net.majatech.ca.exceptions.CaException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class KeyUtilsTest {

    @Test
    public void testRSAKeysAreCreated() {
        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = KeyUtils.generateKeyPair();
        assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("RSA");
        assertThat(keyPair.getPrivate().getAlgorithm()).isEqualTo("RSA");

        Security.removeProvider("BC");
    }

    @Test
    public void testExceptionThrownIfBCProviderNotAdded() {
        try {
            KeyUtils.generateKeyPair();
            fail("No exception thrown");
        } catch (CaException e) {
            assertThat(e.getCause()).isInstanceOf(NoSuchProviderException.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPublicAndPrivateKeysMatch() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = KeyUtils.generateKeyPair(4096);

         byte[] challenge = new byte[10000];
        ThreadLocalRandom.current().nextBytes(challenge);

        // Sign using the private key
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(challenge);

        byte[] signed = signature.sign();

        // Verify signature using the public key
        signature.initVerify(keyPair.getPublic());
        signature.update(challenge);

        assertThat(signature.verify(signed)).isTrue();

        Security.removeProvider("BC");
    }
}
