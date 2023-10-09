package group.aelysium.rustyconnector.core.lib.hash;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;

public class MD5 {
    /**
     * Hash a string as an md5 equivalent.
     * @param string The string to be hashed.
     * @return A md5 hash of the string.
     */
    public static String hash(String string)  {
        return DigestUtils.md5Hex(string).toLowerCase();
    }

    /**
     * Generate a private key.
     * @return A suitable private key.
     */
    public static String generateMD5() {
        Random rand = new Random();
        return hash(String.valueOf(rand.nextInt(257) + rand.nextInt(257) + rand.nextInt(257)));
    }
}
