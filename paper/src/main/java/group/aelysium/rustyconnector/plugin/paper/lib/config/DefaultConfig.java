package group.aelysium.rustyconnector.plugin.paper.lib.config;

import java.io.File;

public class DefaultConfig extends YAML {

    private static DefaultConfig config;

    private String private_key = "";
    private String public_key = "";
    private String server_name = "";
    private String server_address = "";
    private String server_family = "";
    private int server_weight = 0;
    private int server_playerCap_soft = 20;
    private int server_playerCap_hard = 30;

    private String redis_host = "localhost";
    private int redis_port = 3306;
    private String redis_password = "password";
    private String redis_dataChannel = "rustyConnector-sync";

    private boolean registerOnBoot = true;

    private DefaultConfig(File configPointer, String template) {
        super(configPointer, template);
    }
    public String getPrivate_key() {
        return private_key;
    }

    public String getPublic_key() {
        return public_key;
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

    public String getRedis_host() {
        return redis_host;
    }

    public int getRedis_port() {
        return redis_port;
    }

    public String getRedis_password() {
        return redis_password;
    }

    public String getRedis_dataChannel() {
        return redis_dataChannel;
    }

    public boolean isRegisterOnBoot() {
        return registerOnBoot;
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static DefaultConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static DefaultConfig newConfig(File configPointer, String template) {
        config = new DefaultConfig(configPointer, template);
        return DefaultConfig.getConfig();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.private_key = this.getNode(this.data,"private-key",String.class);
        this.public_key = this.getNode(this.data,"public-key",String.class);

        this.server_name = this.getNode(this.data,"server.name",String.class);
        this.server_address = this.getNode(this.data,"server.address",String.class);
        this.server_family = this.getNode(this.data,"server.family",String.class);
        this.server_weight = this.getNode(this.data,"server.weight",Integer.class);
        this.server_playerCap_soft = this.getNode(this.data,"server.player-cap.soft",Integer.class);
        this.server_playerCap_hard = this.getNode(this.data,"server.player-cap.hard",Integer.class);

        this.redis_host = this.getNode(this.data,"redis.host",String.class);
        this.redis_port = this.getNode(this.data,"redis.port",Integer.class);
        this.redis_password = this.getNode(this.data,"redis.password",String.class);
        this.redis_dataChannel = this.getNode(this.data,"redis.data-channel",String.class);

        this.registerOnBoot = this.getNode(this.data,"register-on-boot",Boolean.class);
    }
}
