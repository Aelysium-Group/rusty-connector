package group.aelysium.rustyconnector.plugin.paper.config;

import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class DefaultConfig extends YAML {

    private static DefaultConfig config;

    private String server_name = "";
    private String server_address = "";
    private String server_family = "";
    private int server_weight = 0;
    private int server_playerCap_soft = 20;
    private int server_playerCap_hard = 30;

    private String redis_host = "";
    private int redis_port = 3306;
    private String redis_user = "default";
    private String redis_password = "password";
    private String redis_dataChannel = "rustyConnector-sync";

    private boolean registerOnBoot = true;

    private DefaultConfig(File configPointer, String template) {
        super(configPointer, template);
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

    public String getRedis_user() {
        return redis_user;
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

    public void register() throws IllegalStateException {
        PluginLogger logger = PaperRustyConnector.getAPI().getLogger();

        try {
            this.processVersion(YAML.currentVersion);
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
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Server's soft-cap is either the same as or larger than the server's hard-cap. Running server in player-limit mode."), NamedTextColor.YELLOW);


        // Redis
        this.redis_host = this.getNode(this.data, "redis.host", String.class);
        if(this.redis_host.equals("")) throw new IllegalStateException("Please configure your Redis settings.");

        this.redis_port = this.getNode(this.data, "redis.port", Integer.class);
        this.redis_user = this.getNode(this.data, "redis.user", String.class);
        this.redis_password = this.getNode(this.data, "redis.password", String.class);

        if(this.redis_password.length() != 0 && this.redis_password.length() < 16)
            throw new IllegalStateException("Your Redis password is to short! For security purposes, please use a longer password! "+this.redis_password.length()+" < 16");


        this.redis_dataChannel = this.getNode(this.data, "redis.data-channel", String.class);
        if(this.redis_dataChannel.equals(""))
            throw new IllegalStateException("You must pass a proper name for the data-channel to use with Redis!");

        this.redis_dataChannel = this.getNode(this.data,"redis.data-channel",String.class);
        if(this.redis_dataChannel.equals(""))
            throw new IllegalStateException("You must pass a proper name for the data-channel to use with Redis!");

        this.registerOnBoot = this.getNode(this.data,"register-on-boot",Boolean.class);
    }
}
