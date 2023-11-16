package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;

import java.io.File;

public class MagicLinkConfig extends YAML {
    private String server_family;
    private int server_weight;
    private int server_playerCap_soft;
    private int server_playerCap_hard;

    public MagicLinkConfig(String dataFolder, String configPointer) {
        super(new File(dataFolder, "magic_configs/"+configPointer));
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
        this.server_family = this.getNode(this.data,"family",String.class);
        if(this.server_family.equals("")) throw new IllegalStateException("You must provide a family name in order for RustyConnector to work! The family name must also exist on your Velocity configuration.");

        this.server_weight = this.getNode(this.data,"weight",Integer.class);
        if(this.server_weight < 0) throw new IllegalStateException("Server weight cannot be a negative number.");

        this.server_playerCap_soft = this.getNode(this.data,"player-cap.soft",Integer.class);
        this.server_playerCap_hard = this.getNode(this.data,"player-cap.hard",Integer.class);
        if(this.server_playerCap_soft >= this.server_playerCap_hard) this.server_playerCap_soft = this.server_playerCap_hard;
    }
}
