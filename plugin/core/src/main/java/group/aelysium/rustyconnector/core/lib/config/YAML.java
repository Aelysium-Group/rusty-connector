package group.aelysium.rustyconnector.core.lib.config;

import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class YAML implements IYAML {
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
    public abstract IConfigService.ConfigKey key();

    protected YAML(Path dataFolder, String target, String name) {
        this.configPointer = new File(String.valueOf(dataFolder), target);
        this.target = target;
        this.name = name;
    }

    protected YAML(Path dataFolder, String target, String name, LangService lang, LangFileMappings.Mapping template) {
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
                    stream = IYAML.getResource(lang.code() + "/" + template.path());
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
                this.data = IYAML.loadYAML(this.configPointer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process the version of this config.
     * @throws UnsupportedClassVersionError If the config version doesn't match the plugin version.
     * @throws RuntimeException If the config version is invalid or can't be processed.
     */
    public void processVersion(int currentVersion) {
        try {
            Integer version = IYAML.getValue(this.data, "version", Integer.class);

            if(currentVersion > version)
                throw new UnsupportedClassVersionError("Your configuration file is outdated! " +
                       "(v"+ version +" < v"+ currentVersion +") " +
                       "Please refer to the following link for assistance with upgrading your config! "+MigrationDirections.findUpgradeDirections(version, currentVersion));

            if(currentVersion != version)
                throw new UnsupportedClassVersionError("Your configuration file is from a version of RustyConnector that is newer than the version you currently have installed! We will not provide support for downgrading RustyConnector configs! " +
                        "(v"+ version +" > v"+ currentVersion +")");

            return;
        } catch (IllegalStateException e1) {
            try {
                IYAML.getValue(this.data, "version", String.class);

                throw new RuntimeException("You have set the value of `version` in config.yml to be a string! `version` must be an integer!");
            } catch (IllegalStateException e2) {
                try {
                    IYAML.getValue(this.data, "config-version", Integer.class);

                    throw new UnsupportedClassVersionError("Your configuration file is outdated! " +
                            "(v1 < v"+ currentVersion +") " +
                            "Please refer to the following link for assistance with upgrading your config! "+MigrationDirections.findUpgradeDirections(1, 2));
                } catch (IllegalStateException ignore) {}
            }
        }
        throw new RuntimeException("Could not identify any config version! Make sure that `version` is being used in your `config.yml`!");
    }
}
