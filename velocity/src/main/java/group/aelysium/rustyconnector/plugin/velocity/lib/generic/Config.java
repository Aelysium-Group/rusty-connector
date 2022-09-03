package group.aelysium.rustyconnector.plugin.velocity.lib.generic;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.*;
import java.nio.file.Files;

public class Config extends group.aelysium.rustyconnector.core.generic.lib.generic.Config {
    private VelocityRustyConnector plugin;
    private File configPointer;
    private String template;
    private ConfigurationNode data;

    @Override
    public ConfigurationNode getData() { return this.data; }

    public Config(VelocityRustyConnector plugin, File configPointer, String template) {
        this.plugin = plugin;
        this.configPointer = configPointer;
        this.template = template;
    }

    @Override
    public boolean register() {
        this.plugin.logger().log("---| Registering "+this.configPointer.getName()+"...");
        this.plugin.logger().log("-----| Looking for "+this.configPointer.getName()+"...");

        if (!this.configPointer.exists()) {
            this.plugin.logger().log("-------| "+this.configPointer.getName()+" doesn't exist! Setting it up now...");
            this.plugin.logger().log("-------| Preparing directory...");
            File parent = this.configPointer.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            this.plugin.logger().log("-------| Preparing template file...");
            InputStream templateStream = this.plugin.getResourceAsStream(this.template);
            if (templateStream == null) {
                this.plugin.logger().error("!!!!! Unable to setup "+this.configPointer.getName()+". This config has no template !!!!!");
                return false;
            }

            try {
                this.plugin.logger().log("-------| Cloning template file to new configuration...");
                Files.copy(templateStream, this.configPointer.toPath());
                this.plugin.logger().log("-------| Finished setting up "+this.configPointer.getName());

            } catch (IOException e) {
                this.plugin.logger().error("!!!!! Unable to setup "+this.configPointer.getName()+" !!!!!",e);
                return false;
            }
        } else {
            this.plugin.logger().log("-----| Found it!");
        }

        this.data = this.loadYAML(this.configPointer);
        if(this.data == null) return false;
        return true;
    }

    @Override
    public void reload() {
        int number = 0;
    }

    @Override
    public void save(String data) {
        int number = 0;
    }

    public ConfigurationNode loadYAML(File file) {
        try {
            return YAMLConfigurationLoader.builder()
                    .setIndent(2)
                    .setPath(file.toPath())
                    .build().load();
        } catch (IOException e) {
            this.plugin.logger().error("",e);
            return null;
        }
    }
}
