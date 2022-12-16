package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class YAML extends group.aelysium.rustyconnector.core.lib.config.YAML {
    public YAML(File configPointer, String template) {
        super(configPointer, template);
    }

    @Override
    public boolean generate() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists())
                parent.mkdirs();

            InputStream templateStream = plugin.getResourceAsStream(this.template);
            if (templateStream == null) {
                plugin.logger().error("!!!!! Unable to setup "+this.configPointer.getName()+". This config has no template !!!!!");
                return false;
            }

            try {
                Files.copy(templateStream, this.configPointer.toPath());
            } catch (IOException e) {
                plugin.logger().error("!!!!! Unable to setup "+this.configPointer.getName()+" !!!!!",e);
                return false;
            }
        }

        try {
            this.data = this.loadYAML(this.configPointer);
            if(this.data == null) return false;
            plugin.logger().log("Finished registering "+this.configPointer.getName());
            return true;
        } catch (Exception e) {
            plugin.logger().log("Failed to register: "+this.configPointer.getName());
            return false;
        }
    }
}
