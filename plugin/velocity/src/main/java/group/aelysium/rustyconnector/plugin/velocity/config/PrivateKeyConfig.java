package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PrivateKeyConfig extends YAML {
    protected InputStream data;
    protected InputStream templateStream;
    private PrivateKeyConfig(File configPointer, InputStream templateStream) {
        super(configPointer, "");
        this.templateStream = templateStream;
    }

    public static PrivateKeyConfig newConfig(File configPointer) {
        InputStream stream = new ByteArrayInputStream(MD5.generatePrivateKey().getBytes(StandardCharsets.UTF_8));
        return new PrivateKeyConfig(configPointer, stream);
    }

    @Override
    public boolean generate() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        logger.send(Component.text("Building private.key...", NamedTextColor.DARK_GRAY));

        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            if (this.templateStream == null) {
                logger.error("!!!!! Unable to setup " + this.configPointer.getName() + ". This config has no template !!!!!");
                return false;
            }

            try {
                Files.copy(this.templateStream, this.configPointer.toPath());

            } catch (IOException e) {
                logger.error("!!!!! Unable to setup " + this.configPointer.getName() + " !!!!!", e);
                return false;
            }
        }

        try {
            this.data = new FileInputStream(this.configPointer);
            logger.send(Component.text("Finished building private.key", NamedTextColor.GREEN));
            return true;
        } catch (Exception e) {
            logger.send(Component.text("Failed to build private.key", NamedTextColor.RED));
            return false;
        }
    }

    public char[] get() throws IOException {
        return new String(this.data.readAllBytes(), StandardCharsets.UTF_8).toCharArray();
    }
}
