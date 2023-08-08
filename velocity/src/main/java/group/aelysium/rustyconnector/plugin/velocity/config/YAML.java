package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class YAML extends group.aelysium.rustyconnector.core.lib.config.YAML {
    protected static int currentVersion = 4;
    public YAML(File configPointer, String template) {
        super(configPointer, template);
    }

    @Override
    public boolean generate() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        logger.send(Component.text("Building "+this.configPointer.getName()+"...", NamedTextColor.DARK_GRAY));
        if (!this.configPointer.exists()) {
            File parent = this.configPointer.getParentFile();
            if (!parent.exists())
                parent.mkdirs();

            InputStream templateStream = api.resourceAsStream(this.template);
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
            logger.send(Component.text("Finished building "+this.configPointer.getName(), NamedTextColor.GREEN));
            return true;
        } catch (Exception e) {
            logger.send(Component.text("Failed to build "+this.configPointer.getName(), NamedTextColor.RED));
            return false;
        }
    }
}
