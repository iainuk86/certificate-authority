package net.majatech.ca.utils;

import net.majatech.ca.exceptions.CaException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;

public final class KeyUtils {
    private static final String ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 2048;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private KeyUtils() {}

    public static KeyPair generateKeyPair() {
        return generateKeyPair(DEFAULT_KEY_SIZE);
    }

    public static KeyPair generateKeyPair(int keySize) {
        try {
            final KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM, "BC");
            gen.initialize(keySize);

            return gen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CaException(e.getMessage());
        }
    }
}
