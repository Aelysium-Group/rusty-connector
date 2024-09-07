package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.*;
import group.aelysium.rustyconnector.common.crypt.AES;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Config("metadata/aes.private")
public class PrivateKeyConfig {
    @AllContents()
    private byte[] key;

    public AES cryptor() {
        return AES.from(this.key);
    }

    public static PrivateKeyConfig New() throws IOException {
        // This logic only cares about generating the config if it doesn't exist.
        File file = new File("metadata/aes.private");
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists()) parent.mkdirs();

                try(FileWriter writer = new FileWriter(file)) {
                    writer.write(new String(Base64.getEncoder().encode(AES.createKey()), StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ConfigLoader.load(PrivateKeyConfig.class);
    }
}
