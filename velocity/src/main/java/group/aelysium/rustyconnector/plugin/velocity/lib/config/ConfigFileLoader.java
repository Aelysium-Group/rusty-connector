package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.*;
import java.nio.file.Files;

public class ConfigFileLoader extends group.aelysium.rustyconnector.core.lib.Config {
    private File configPointer;
    private String template;
    private ConfigurationNode data;

    @Override
    public ConfigurationNode getData() { return this.data; }

    public ConfigFileLoader(File configPointer, String template) {
        this.configPointer = configPointer;
        this.template = template;
    }

    public String getName() {
        return this.configPointer.getName();
    }

    @Override
    public boolean generate() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        plugin.logger().log("---| Registering "+this.configPointer.getName()+"...");
        plugin.logger().log("-----| Looking for "+this.configPointer.getName()+"...");

        if (!this.configPointer.exists()) {
            plugin.logger().log("-------| "+this.configPointer.getName()+" doesn't exist! Setting it up now...");
            plugin.logger().log("-------| Preparing directory...");
            File parent = this.configPointer.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            plugin.logger().log("-------| Preparing template file...");
            InputStream templateStream = plugin.getResourceAsStream(this.template);
            if (templateStream == null) {
                plugin.logger().error("!!!!! Unable to setup "+this.configPointer.getName()+". This config has no template !!!!!");
                return false;
            }

            try {
                plugin.logger().log("-------| Cloning template file to new configuration...");
                Files.copy(templateStream, this.configPointer.toPath());
                plugin.logger().log("-------| Finished setting up "+this.configPointer.getName());

            } catch (IOException e) {
                plugin.logger().error("!!!!! Unable to setup "+this.configPointer.getName()+" !!!!!",e);
                return false;
            }
        } else {
            plugin.logger().log("-----| Found it!");
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
            VelocityRustyConnector.getInstance().logger().error("",e);
            return null;
        }
    }
}
