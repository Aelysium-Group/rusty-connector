package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class DefaultConfig extends YAML {
    private static DefaultConfig config;
    private boolean debug = false;

    private String redis_host = "";
    private int redis_port = 3306;
    private String redis_user = "default";
    private String redis_password = "password";
    private String redis_dataChannel = "rustyConnector-sync";

    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    private Integer services_serverLifecycle_serverTimeout = 15;
    private Integer services_serverLifecycle_serverPingInterval = 10;
    private Boolean services_loadBalancing_enabled = true;
    private Integer services_loadBalancing_interval = 20;

    private DefaultConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static DefaultConfig config() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static DefaultConfig newConfig(File configPointer, String template) {
        config = new DefaultConfig(configPointer, template);
        return DefaultConfig.config();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    public boolean shouldDebug() {
        return this.debug;
    }

    public String redis_host() {
        return this.redis_host;
    }

    public int redis_port() {
        return this.redis_port;
    }

    public String redis_password() {
        return this.redis_password;
    }

    public String redis_user() {
        return this.redis_user;
    }

    public String redis_dataChannel() {
        return this.redis_dataChannel;
    }

    public boolean whitelist_enabled() {
        return this.whitelist_enabled;
    }

    public String whitelist_name() {
        return this.whitelist_name;
    }

    public Integer services_serverLifecycle_serverTimeout() {
        return services_serverLifecycle_serverTimeout;
    }

    public Integer services_serverLifecycle_serverPingInterval() {
        return services_serverLifecycle_serverPingInterval;
    }

    public Integer services_loadBalancing_interval() {
        return services_loadBalancing_interval;
    }

    public Boolean services_loadBalancing_enabled() {
        return services_loadBalancing_enabled;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = VelocityAPI.get().logger();

        try {
            this.processVersion(YAML.currentVersion);
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        try {
            this.debug = this.getNode(this.data,"debug",Boolean.class);
        } catch (Exception e) {
            this.debug = false;
        }

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

        // Whitelist

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.name cannot be empty in order to use a whitelist on the proxy!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");

        // Hearts
        this.services_serverLifecycle_serverTimeout = this.getNode(this.data,"services.server-lifecycle.server-timeout",Integer.class);
        if(this.services_serverLifecycle_serverTimeout < 5) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Server timeout is set dangerously fast: " + this.services_serverLifecycle_serverTimeout + "s. Setting to default of 5s."), NamedTextColor.YELLOW);
            this.services_serverLifecycle_serverTimeout= 5;
        }
        this.services_serverLifecycle_serverPingInterval = this.getNode(this.data,"services.server-lifecycle.server-ping-interval",Integer.class);
        if(this.services_serverLifecycle_serverPingInterval < 5) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Server ping interval is set dangerously fast: " + this.services_serverLifecycle_serverPingInterval + "s. Setting to default of 5s."), NamedTextColor.YELLOW);
            this.services_serverLifecycle_serverPingInterval = 5;
        }
        if(this.services_serverLifecycle_serverTimeout < this.services_serverLifecycle_serverPingInterval) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Server timeout can't be less that server ping interval!"), NamedTextColor.YELLOW);
            this.services_serverLifecycle_serverPingInterval = this.services_serverLifecycle_serverTimeout - 2;
        }


        this.services_loadBalancing_enabled = this.getNode(this.data,"services.load-balancing.enabled",Boolean.class);
        this.services_loadBalancing_interval = this.getNode(this.data,"services.load-balancing.interval",Integer.class);
        if(this.services_loadBalancing_interval < 7) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Server sorting interval is set dangerously fast: " + this.services_loadBalancing_interval + "ms. Setting to default of 20ms."), NamedTextColor.YELLOW);
            this.services_loadBalancing_interval = 20;
        }
    }
}
