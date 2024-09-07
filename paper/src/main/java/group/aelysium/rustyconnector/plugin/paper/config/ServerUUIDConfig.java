package group.aelysium.rustyconnector.plugin.paper.config;

import group.aelysium.rustyconnector.common.config.AllContents;
import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.config.ConfigLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Config("metadata/aes.private")
public class ServerUUIDConfig {
    @AllContents()
    private byte[] key;

    public UUID uuid() {
        return UUID.fromString(new String(key, StandardCharsets.UTF_8));
    }

    public static ServerUUIDConfig New() throws IOException {
        // This logic only cares about generating the config if it doesn't exist.
        File file = new File("metadata/aes.private");
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists()) parent.mkdirs();

                try(FileWriter writer = new FileWriter(file)) {
                    writer.write(UUID.randomUUID().toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ConfigLoader.load(ServerUUIDConfig.class);
    }
}
