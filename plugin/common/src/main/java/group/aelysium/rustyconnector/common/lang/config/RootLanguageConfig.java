package group.aelysium.rustyconnector.common.lang.config;

import group.aelysium.rustyconnector.toolkit.common.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.logger.IPluginLogger;
import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.exception.NoOutputException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class RootLanguageConfig extends Config {
    protected RootLanguageConfig(Path dataFolder) {
        super(dataFolder, "language.yml", "language");
    }

    protected String language;
    public String getLanguage() { return this.language; }

    protected void generate(IPluginLogger logger) throws Exception {
        logger.send(Component.text("Building "+this.configPointer.getName()+"...", NamedTextColor.DARK_GRAY));
        try {
            if (!this.configPointer.exists()) {
                File parent = this.configPointer.getParentFile();
                if (!parent.exists())
                    parent.mkdirs();

                try {
                    InputStream stream = Config.class.getClassLoader().getResourceAsStream("language.yml");
                    Files.copy(stream, this.configPointer.toPath());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                this.data = IConfig.loadYAML(this.configPointer);
                if (this.data == null) throw new NullPointerException();
                logger.send(Component.text("Finished building " + this.configPointer.getName(), NamedTextColor.GREEN));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void register() throws IllegalStateException, NoOutputException {
        try {
            this.language = IConfig.getValue(this.data,"language", String.class);
        } catch (Exception e) {
            this.language = "en_us";
        }
    }

    public static RootLanguageConfig construct(Path dataFolder) {
        RootLanguageConfig config = new RootLanguageConfig(dataFolder);
        config.register();
        return config;
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(RootLanguageConfig.class);
    }
}
