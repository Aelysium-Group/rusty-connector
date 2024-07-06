package group.aelysium.websocket_bridge;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESCryptor {
    private final SecretKey key;
    private static final int DATA_LENGTH = 128;
    private Cipher encryptionCipher;

    public AESCryptor(SecretKey key) {
        this.key = key;
    }

    private String encode(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private byte[] decode(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    public String encrypt(String data) throws Exception {
        byte[] dataInBytes = data.getBytes();
        encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = encryptionCipher.doFinal(dataInBytes);
        return encode(encryptedBytes);
    }

    public String decrypt(String encryptedData) throws Exception {
        byte[] dataInBytes = decode(encryptedData);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(DATA_LENGTH, encryptionCipher.getIV());
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decryptedBytes = decryptionCipher.doFinal(dataInBytes);
        return new String(decryptedBytes);
    }

    public static AESCryptor create(String key) throws IllegalArgumentException {
        if(key.length() % 2 != 0) throw new IllegalArgumentException("Key must be an even number!");
        if(key.length() < 16) throw new IllegalArgumentException("Key must be at least 16 characters!");

        byte[] decodedString = key.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(decodedString, 0, decodedString.length, "AES");
        return new AESCryptor(secretKey);
    }
}