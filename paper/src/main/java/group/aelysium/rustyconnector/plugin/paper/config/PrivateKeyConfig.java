package group.aelysium.rustyconnector.plugin.paper.config;

import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PrivateKeyConfig extends YAML {
    protected InputStream data;
    protected InputStream templateStream;
    private PrivateKeyConfig(File configPointer, InputStream templateStream) {
        super(configPointer, "");
        this.templateStream = templateStream;
    }

    public static PrivateKeyConfig newConfig(File configPointer) {
        InputStream stream = new ByteArrayInputStream(MD5.generatePrivateKey().getBytes(StandardCharsets.UTF_8));
        return new PrivateKeyConfig(configPointer, stream);
    }

    @Override
    public boolean generate() {
        PaperAPI api = PaperAPI.get();
        PluginLogger logger = api.logger();

        logger.log("---| Registering "+this.configPointer.getName()+"...");
        logger.log("-----| Looking for "+this.configPointer.getName()+"...");

        if (!this.configPointer.exists()) {
            logger.log("-------| "+this.configPointer.getName()+" doesn't exist! Setting it up now...");
            logger.log("-------| Preparing directory...");
            File parent = this.configPointer.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            if (this.templateStream == null) {
                logger.error("!!!!! Unable to setup "+this.configPointer.getName()+". This config has no template !!!!!");
                return false;
            }

            try {
                logger.log("-------| Cloning template file to new configuration...");
                Files.copy(this.templateStream, this.configPointer.toPath());
                logger.log("-------| Finished setting up "+this.configPointer.getName());

            } catch (IOException e) {
                logger.error("!!!!! Unable to setup "+this.configPointer.getName()+" !!!!!",e);
                return false;
            }
        } else {
            logger.log("-----| Found it!");
        }

        try {
            this.data = new FileInputStream(this.configPointer);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public char[] get() throws IOException {
        return new String(this.data.readAllBytes(), StandardCharsets.UTF_8).toCharArray();
    }
}