package group.aelysium.rustyconnector.core.plugin.central.config;

import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class DefaultConfig extends YAML {
    private String server_name;
    private String server_address;
    private String server_family;
    private int server_weight;
    private int server_playerCap_soft;
    private int server_playerCap_hard;

    public DefaultConfig(File configPointer) {
        super(configPointer);
    }
    public String getServer_name() {
        return server_name;
    }

    public String getServer_address() {
        return server_address;
    }

    public String getServer_family() {
        return server_family;
    }

    public int getServer_weight() {
        return server_weight;
    }

    public int getServer_playerCap_soft() {
        return server_playerCap_soft;
    }

    public int getServer_playerCap_hard() {
        return server_playerCap_hard;
    }

    public void register(int configVersion) throws IllegalStateException {
        PluginLogger logger = Plugin.getAPI().logger();

        try {
            this.processVersion(configVersion);
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        this.server_name = this.getNode(this.data,"server.name",String.class);
        if(this.server_name.equals("")) throw new IllegalStateException("You must provide a server name in order for RustyConnector to work!");

        this.server_address = this.getNode(this.data,"server.address",String.class);
        if(this.server_address.equals("")) throw new IllegalStateException("You must provide a server address in order for RustyConnector to work! Addresses should also include a port number if necessary.");

        this.server_family = this.getNode(this.data,"server.family",String.class);
        if(this.server_family.equals("")) throw new IllegalStateException("You must provide a family name in order for RustyConnector to work! The family name must also exist on your Velocity configuration.");

        this.server_weight = this.getNode(this.data,"server.weight",Integer.class);
        if(this.server_weight < 0) throw new IllegalStateException("Server weight cannot be a negative number.");

        this.server_playerCap_soft = this.getNode(this.data,"server.player-cap.soft",Integer.class);
        this.server_playerCap_hard = this.getNode(this.data,"server.player-cap.hard",Integer.class);
        if(this.server_playerCap_soft >= this.server_playerCap_hard)
            PluginLang.BOXED_MESSAGE_COLORED.send(logger, "Server's soft-cap is either the same as or larger than the server's hard-cap. Running server in player-limit mode.", NamedTextColor.YELLOW);
    }
}
