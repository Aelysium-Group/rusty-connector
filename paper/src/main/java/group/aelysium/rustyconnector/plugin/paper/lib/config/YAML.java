package group.aelysium.rustyconnector.plugin.paper.lib.config;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;

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
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        logger.log("---| Registering "+this.configPointer.getName()+"...");
        logger.log("-----| Looking for "+this.configPointer.getName()+"...");

        if (!this.configPointer.exists()) {
            logger.log("-------| "+this.configPointer.getName()+" doesn't exist! Setting it up now...");
            logger.log("-------| Preparing directory...");
            File parent = this.configPointer.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            logger.log("-------| Preparing template file...");
            InputStream templateStream = api.getResourceAsStream(this.template);
            if (templateStream == null) {
                logger.error("!!!!! Unable to setup "+this.configPointer.getName()+". This config has no template !!!!!");
                return false;
            }

            try {
                logger.log("-------| Cloning template file to new configuration...");
                Files.copy(templateStream, this.configPointer.toPath());
                logger.log("-------| Finished setting up "+this.configPointer.getName());

            } catch (IOException e) {
                logger.error("!!!!! Unable to setup "+this.configPointer.getName()+" !!!!!",e);
                return false;
            }
        } else {
            logger.log("-----| Found it!");
        }

        try {
            this.data = this.loadYAML(this.configPointer);
            return !(this.data == null);
        } catch (Exception e) {
            return false;
        }
    }
}
