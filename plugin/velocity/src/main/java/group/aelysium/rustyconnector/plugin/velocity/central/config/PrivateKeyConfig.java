package group.aelysium.rustyconnector.plugin.velocity.central.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PrivateKeyConfig extends YAML {
    protected InputStream data;
    private PrivateKeyConfig(File configPointer) {
        super(configPointer, "");
    }

    public static PrivateKeyConfig newConfig(File configPointer) {
        return new PrivateKeyConfig(configPointer);
    }

    public boolean generateFilestream(List<Component> outputLog) {
        outputLog.add(Component.text("Building "+this.configPointer.getName()+"...", NamedTextColor.DARK_GRAY));
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists())
                parent.mkdirs();

            try {
                InputStream stream = new ByteArrayInputStream(AESCryptor.createKey());
                Files.copy(stream, this.configPointer.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Unable to setup "+this.configPointer.getName()+"! No further information.");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            this.data = new FileInputStream(this.configPointer);
            outputLog.add(Component.text("Finished building "+this.configPointer.getName(), NamedTextColor.GREEN));
            return true;
        } catch (Exception e) {
            outputLog.add(Component.text("Failed to build "+this.configPointer.getName(), NamedTextColor.RED));
            return false;
        }
    }

    public AESCryptor get() throws IOException {
        return AESCryptor.from(this.data.readAllBytes());
    }
}
