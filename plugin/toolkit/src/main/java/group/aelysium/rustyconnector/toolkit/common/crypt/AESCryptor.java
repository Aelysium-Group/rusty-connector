package group.aelysium.rustyconnector.toolkit.common.crypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESCryptor {
    private final SecretKey key;

    public AESCryptor(SecretKey key) {
        this.key = key;
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, this.key);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    public static byte[] createKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey.getEncoded();
    }

    public static AESCryptor from(byte[] key) {
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        return new AESCryptor(secretKey);
    }
}