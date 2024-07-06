package group.aelysium.rustyconnector.common.config;

import group.aelysium.rustyconnector.toolkit.common.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Config<Setting> implements IConfig<Setting> {
    protected String name;
    protected String target;
    protected File configPointer;
    protected ConfigurationNode data;

    public ConfigurationNode nodes() { return this.data; }
    public String name() {
        return this.name;
    }
    public String fileTarget() {
        return this.target;
    }

    protected Config(Path dataFolder, String target, String name) {
        this.configPointer = new File(String.valueOf(dataFolder), target);
        this.target = target;
        this.name = name;
    }

    protected Config(Path dataFolder, String target, String name, LangFileMappings.Mapping template) {
        this.configPointer = new File(String.valueOf(dataFolder), target);
        this.target = target;
        this.name = name;

        try {
            if (!this.configPointer.exists()) {
                File parent = this.configPointer.getParentFile();
                if (!parent.exists())
                    parent.mkdirs();

                InputStream stream;
                if (lang.isInline())
                    stream = IConfig.getResource(lang.code() + "/" + template.path());
                else
                    stream = new FileInputStream(lang.get(template));

                try {
                    Files.copy(stream, this.configPointer.toPath());
                } catch (IOException e) {
                    throw new RuntimeException("Unable to setup " + this.configPointer.getName() + "! No further information.");
                }

                stream.close();
            }

            try {
                this.data = IConfig.loadYAML(this.configPointer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
