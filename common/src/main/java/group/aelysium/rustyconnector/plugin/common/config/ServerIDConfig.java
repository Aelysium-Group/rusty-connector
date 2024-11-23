package group.aelysium.rustyconnector.plugin.common.config;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.rustyconnector.common.crypt.NanoID;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Config("plugins/rustyconnector/metadata/server.id")
public class ServerIDConfig {
    @AllContents()
    private byte[] id;

    public String id() {
        return new String(id, StandardCharsets.UTF_8);
    }

    public static ServerIDConfig New() throws IOException {
        // This logic only cares about generating the config if it doesn't exist.
        File file = new File("plugins/rustyconnector/metadata/server.id");
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if(!parent.exists()) parent.mkdirs();

                try(FileWriter writer = new FileWriter(file)) {
                    writer.write(NanoID.randomNanoID().toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return DeclarativeYAML.load(ServerIDConfig.class);
    }
}
