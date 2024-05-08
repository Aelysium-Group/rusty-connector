package group.aelysium.rustyconnector.core.lib.config.common;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class UUIDConfig {
    protected InputStream data;
    protected File configPointer;
    public UUIDConfig(File configPointer) {
        this.configPointer = configPointer;
    }

    public UUID get(List<Component> outputLog) {
        outputLog.add(Component.text("Fetching system UUID...", NamedTextColor.DARK_GRAY));
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists())
                parent.mkdirs();

            try {
                InputStream stream = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
                Files.copy(stream, this.configPointer.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Unable to find system UUID! No further information.");
            }
        }

        try {
            this.data = new FileInputStream(this.configPointer);
            outputLog.add(Component.text("Finished finding system UUID!", NamedTextColor.GREEN));
            return UUID.fromString(new String(this.data.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
