package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class YAML extends group.aelysium.rustyconnector.core.lib.config.YAML {
    protected static int currentVersion = 3;
    public YAML(File configPointer, String template) {
        super(configPointer, template);
    }

    @Override
    public boolean generate() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists())
                parent.mkdirs();

            InputStream templateStream = api.getResourceAsStream(this.template);
            if (templateStream == null) {
                logger.error("!!!!! Unable to setup "+this.configPointer.getName()+". This config has no template !!!!!");
                return false;
            }

            try {
                Files.copy(templateStream, this.configPointer.toPath());
            } catch (IOException e) {
                logger.error("!!!!! Unable to setup "+this.configPointer.getName()+" !!!!!",e);
                return false;
            }
        }

        try {
            this.data = this.loadYAML(this.configPointer);
            if(this.data == null) return false;
            logger.log("Finished registering "+this.configPointer.getName());
            return true;
        } catch (Exception e) {
            logger.log("Failed to register: "+this.configPointer.getName());
            return false;
        }
    }
}
