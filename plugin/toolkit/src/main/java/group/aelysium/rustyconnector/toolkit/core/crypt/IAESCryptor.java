package group.aelysium.rustyconnector.toolkit.core.crypt;

public interface IAESCryptor {
    /**
     * Encrypts a string of data.
     * @param data The data to encrypt.
     * @return The encrypted data.
     * @throws Exception If there was an issue while encrypting.
     */
    String encrypt(String data) throws Exception;

    /**
     * Decrypts a string of data.
     * @param encryptedData The data to decrypt.
     * @return The decrypted data.
     * @throws Exception If there was an issue while decrypting.
     */
    String decrypt(String encryptedData) throws Exception;
}
