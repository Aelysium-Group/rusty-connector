package group.aelysium.rustyconnector.plugin.paper.config;

import group.aelysium.rustyconnector.common.config.AllContents;
import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.config.ConfigLoader;
import group.aelysium.rustyconnector.common.crypt.AES;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Config("plugins/rustyconnector/metadata/aes.private")
public class PrivateKeyConfig {
    @AllContents()
    private byte[] key;

    public AES cryptor() {
        System.out.println(new String(key, StandardCharsets.UTF_8));
        return AES.from(Base64.getDecoder().decode(this.key));
    }

    public static PrivateKeyConfig New() throws IOException {
        // This logic only cares about generating the config if it doesn't exist.
        File file = new File("plugins/rustyconnector/metadata/aes.private");
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists()) parent.mkdirs();

                try(FileWriter writer = new FileWriter(file)) {
                    writer.write(new String(Base64.getEncoder().encode(AES.createKey()), StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return ConfigLoader.load(PrivateKeyConfig.class);
    }
}
