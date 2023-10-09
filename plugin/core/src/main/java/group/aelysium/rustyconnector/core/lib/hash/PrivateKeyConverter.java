package group.aelysium.rustyconnector.core.lib.hash;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class PrivateKeyConverter {

    /**
     * Convert a string to a private key
     * @param value The string to convert.
     * @return The private key.
     * @throws GeneralSecurityException
     */
    public static PrivateKey valueOf(String value) throws GeneralSecurityException {
        byte[] clear = value.getBytes(StandardCharsets.UTF_8);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("DSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    public static String toString(PublicKey key) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("DSA");
        PKCS8EncodedKeySpec spec = fact.getKeySpec(key, PKCS8EncodedKeySpec.class);
        byte[] packed = spec.getEncoded();
        return new String(packed, StandardCharsets.UTF_8);
    }


}
