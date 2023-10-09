package group.aelysium.rustyconnector.core.lib.hash;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class PublicKeyConverter {

    /**
     * Convert a string to a public key.
     * @param value The string to convert.
     * @return The public key.
     * @throws GeneralSecurityException
     */
    public static java.security.PublicKey valueOf(String value) throws GeneralSecurityException {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("DSA");
        return fact.generatePublic(spec);
    }

    public static String toString(PublicKey key) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("DSA");
        X509EncodedKeySpec spec = fact.getKeySpec(key, X509EncodedKeySpec.class);
        return new String(spec.getEncoded(),StandardCharsets.UTF_8);
    }
}
