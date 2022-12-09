package group.aelysium.rustyconnector.plugin.paper.lib.config;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class YAML extends group.aelysium.rustyconnector.core.lib.parsing.YAML {
    public YAML(File configPointer, String template) {
        super(configPointer, template);
    }

    @Override
    public boolean generate() {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

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

        try {
            this.data = this.loadYAML(this.configPointer);
            return !(this.data == null);
        } catch (Exception e) {
            return false;
        }
    }
}
