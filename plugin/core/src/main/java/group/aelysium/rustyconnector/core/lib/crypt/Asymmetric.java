package group.aelysium.rustyconnector.core.lib.crypt;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Asymmetric {
    private static final String RSA = "RSA";
    public static KeyPair generateKeys() throws Exception {
        SecureRandom secureRandom = new SecureRandom();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);

        keyPairGenerator.initialize(2048, secureRandom);

        return keyPairGenerator.generateKeyPair();
    }

    public static String encrypt(String plainText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return new String(cipher.doFinal(plainText.getBytes()), StandardCharsets.UTF_8);
    }

    public static String decrypt(String cipherText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(cipherText.getBytes(StandardCharsets.UTF_8));

        return new String(result);
    }
}