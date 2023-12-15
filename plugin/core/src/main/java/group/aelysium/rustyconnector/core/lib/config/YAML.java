package group.aelysium.rustyconnector.core.lib.config;

import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public abstract class YAML {
    protected File configPointer;
    protected ConfigurationNode data;

    public ConfigurationNode getData() { return this.data; }

    protected YAML(Path dataFolder, String target) {
        this.configPointer = new File(String.valueOf(dataFolder), target);
    }

    protected YAML(Path dataFolder, String target, LangService lang, LangFileMappings.Mapping template) {
        this.configPointer = new File(String.valueOf(dataFolder), target);

        try {
            if (!this.configPointer.exists()) {
                File parent = this.configPointer.getParentFile();
                if (!parent.exists())
                    parent.mkdirs();

                InputStream stream;
                if (lang.isInline())
                    stream = YAML.getResource(lang.code() + "/" + template.path());
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
                this.data = this.loadYAML(this.configPointer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return this.configPointer.getName();
    }
    protected static ConfigurationNode get(ConfigurationNode node, String path) {
        String[] steps = path.split("\\.");

        final ConfigurationNode[] currentNode = {node};
        Arrays.stream(steps).forEach(step -> {
            currentNode[0] = currentNode[0].getNode(step);
        });

        if(currentNode[0] == null) throw new IllegalArgumentException("The called YAML node `"+path+"` was null.");

        return currentNode[0];
    }

    /**
     * Retrieve data from a specific configuration node.
     * @param data The configuration data to search for a specific node.
     * @param node The node to search for.
     * @param type The type to convert the retrieved data to.
     * @return Data with a type matching `type`
     * @throws IllegalStateException If there was an issue while retrieving the data or converting it to `type`.
     */
    protected <T> T getNode(ConfigurationNode data, String node, Class<? extends T> type) throws IllegalStateException {
        try {
            Object objectData = YAML.get(data,node).getValue();
            if(objectData == null) throw new NullPointerException();

            return type.cast(objectData);
        } catch (NullPointerException e) {
            throw new IllegalStateException("The node ["+node+"] is missing!");
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node ["+node+"] is of the wrong data type! Make sure you are using the correct type of data!");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to register the node: "+node);
        }
    }

    public static InputStream getResource(String path) {
        return YAML.class.getClassLoader().getResourceAsStream(path);
    }

    protected ConfigurationNode loadYAML(File file) throws IOException {
        return YAMLConfigurationLoader.builder()
                .setIndent(2)
                .setPath(file.toPath())
                .build().load();
    }

    /**
     * Process the version of this config.
     * @throws UnsupportedClassVersionError If the config version doesn't match the plugin version.
     * @throws RuntimeException If the config version is invalid or can't be processed.
     */
    public void processVersion(int currentVersion) {
        try {
            Integer version = this.getNode(this.data, "version", Integer.class);

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
                this.getNode(this.data, "version", String.class);

                throw new RuntimeException("You have set the value of `version` in config.yml to be a string! `version` must be an integer!");
            } catch (IllegalStateException e2) {
                try {
                    this.getNode(this.data, "config-version", Integer.class);

                    throw new UnsupportedClassVersionError("Your configuration file is outdated! " +
                            "(v1 < v"+ currentVersion +") " +
                            "Please refer to the following link for assistance with upgrading your config! "+MigrationDirections.findUpgradeDirections(1, 2));
                } catch (IllegalStateException ignore) {}
            }
        }
        throw new RuntimeException("Could not identify any config version! Make sure that `version` is being used in your `config.yml`!");
    }
}
