package group.aelysium.rustyconnector.plugin.paper.central.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

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

    public boolean generateFilestream(List<Component> outputLog) {
        outputLog.add(Component.text("Building "+this.configPointer.getName()+"...", NamedTextColor.DARK_GRAY));
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            InputStream templateStream = getClass().getClassLoader().getResourceAsStream(this.template);
            if (templateStream == null)
                throw new RuntimeException("Unable to setup \"+this.configPointer.getName()+\". This config has no template !");

            try {
                Files.copy(templateStream, this.configPointer.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Unable to setup "+this.configPointer.getName()+"! No further information.");
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

    public char[] get() throws IOException {
        return new String(this.data.readAllBytes(), StandardCharsets.UTF_8).toCharArray();
    }
}