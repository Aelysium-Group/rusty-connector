package group.aelysium.rustyconnector.plugin.velocity.central.config;

import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.mcloader.lib.lang.PluginLang;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class MagicDefaultConfig extends YAML {
    private String server_family;
    private int server_weight;
    private int server_playerCap_soft;
    private int server_playerCap_hard;

    public MagicDefaultConfig(String dataFolder, String configPointer) {
        super(new File(dataFolder, "magic_configs/"+configPointer+".yml"));
    }

    public String family() {
        return server_family;
    }

    public int weight() {
        return server_weight;
    }

    public int playerCap_soft() {
        return server_playerCap_soft;
    }

    public int playerCap_hard() {
        return server_playerCap_hard;
    }

    public void register() throws IllegalStateException {
        PluginLogger logger = TinderAdapterForCore.getTinder().logger();

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
