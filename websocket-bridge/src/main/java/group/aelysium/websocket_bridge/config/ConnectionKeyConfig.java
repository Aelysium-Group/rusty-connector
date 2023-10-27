package group.aelysium.rustyconnector.core.lib.key.config;

import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PrivateKeyConfig {
    protected InputStream data;
    protected File configPointer;
    public PrivateKeyConfig(File configPointer) {
        this.configPointer = configPointer;
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
