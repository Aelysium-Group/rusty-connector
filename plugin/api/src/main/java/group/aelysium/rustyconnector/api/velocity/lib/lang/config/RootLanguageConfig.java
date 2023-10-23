package group.aelysium.rustyconnector.api.velocity.lib.lang.config;

import group.aelysium.rustyconnector.api.velocity.lib.PluginLogger;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.file.Files;

public class RootLanguageConfig extends YAML {
    public RootLanguageConfig(File configPointer) {
        super(configPointer);
    }

    protected String language;
    public String getLanguage() { return this.language; }

    public boolean generate(PluginLogger logger) throws Exception {
        logger.send(Component.text("Building "+this.configPointer.getName()+"...", NamedTextColor.DARK_GRAY));
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists())
                parent.mkdirs();

            try {
                InputStream stream = YAML.class.getClassLoader().getResourceAsStream("language.yml");
                Files.copy(stream, this.configPointer.toPath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            this.data = this.loadYAML(this.configPointer);
            if(this.data == null) return false;
            logger.send(Component.text("Finished building "+this.configPointer.getName(), NamedTextColor.GREEN));

            return true;
        } catch (Exception e) {
            logger.send(Component.text("Failed to build "+this.configPointer.getName(), NamedTextColor.RED));

            return false;
        }
    }

    public void register() throws IllegalStateException, NoOutputException {
        try {
            this.language = this.getNode(this.data,"language", String.class);
        } catch (Exception e) {
            this.language = "en_us";
        }
    }
}
