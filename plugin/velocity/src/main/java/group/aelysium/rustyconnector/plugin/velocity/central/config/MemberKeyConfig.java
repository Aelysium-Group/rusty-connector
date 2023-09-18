package group.aelysium.rustyconnector.plugin.velocity.central.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class MemberKeyConfig extends YAML {
    protected InputStream data;
    protected InputStream templateStream;
    private MemberKeyConfig(File configPointer, InputStream templateStream) {
        super(configPointer);
        this.templateStream = templateStream;
    }

    public static MemberKeyConfig newConfig(File configPointer) {
        InputStream stream = new ByteArrayInputStream(MD5.generateMD5().getBytes(StandardCharsets.UTF_8));
        return new MemberKeyConfig(configPointer, stream);
    }

    public char[] get() throws IOException {
        return new String(this.data.readAllBytes(), StandardCharsets.UTF_8).toCharArray();
    }
}
