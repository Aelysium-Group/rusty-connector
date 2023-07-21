package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private int messageTunnel_messageCacheSize = 50;
    private int messageTunnel_messageMaxLength = 512;
    private boolean messageTunnel_whitelist_enabled = false;
    private List<String> messageTunnel_whitelist_addresses = new ArrayList<>();
    private boolean messageTunnel_denylist_enabled = false;
    private List<String> messageTunnel_denylist_addresses = new ArrayList<>();

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

    public boolean shouldDebug() {
        return this.debug;
    }

    public String getRedis_host() {
        return this.redis_host;
    }

    public int getRedis_port() {
        return this.redis_port;
    }

    public String getRedis_password() {
        return this.redis_password;
    }

    public String getRedis_user() {
        return this.redis_user;
    }

    public String getRedis_dataChannel() {
        return this.redis_dataChannel;
    }

    public boolean isWhitelist_enabled() {
        return this.whitelist_enabled;
    }

    public String getWhitelist_name() {
        return this.whitelist_name;
    }

    public int getMessageTunnel_messageCacheSize() {
        return messageTunnel_messageCacheSize;
    }

    public int getMessageTunnel_messageMaxLength() {
        return messageTunnel_messageMaxLength;
    }

    public List<String> getMessageTunnel_whitelist_addresses() {
        return this.messageTunnel_whitelist_addresses;
    }

    public boolean isMessageTunnel_whitelist_enabled() {
        return this.messageTunnel_whitelist_enabled;
    }

    public List<String> getMessageTunnel_denylist_addresses() {
        return this.messageTunnel_denylist_addresses;
    }
    public boolean isMessageTunnel_denylist_enabled() {
        return this.messageTunnel_denylist_enabled;
    }

    public Integer getServices_serverLifecycle_serverTimeout() {
        return services_serverLifecycle_serverTimeout;
    }

    public Integer getServices_serverLifecycle_serverPingInterval() {
        return services_serverLifecycle_serverPingInterval;
    }

    public Integer getServices_loadBalancing_interval() {
        return services_loadBalancing_interval;
    }

    public Boolean getServices_loadBalancing_enabled() {
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

        // Message tunnel

        this.messageTunnel_messageCacheSize = this.getNode(this.data,"message-tunnel.message-cache-size",Integer.class);
        if(this.messageTunnel_messageCacheSize > 500) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Message cache size is to large! " + this.messageTunnel_messageCacheSize + " > 500. Message cache size set to 500."), NamedTextColor.YELLOW);
            this.messageTunnel_messageCacheSize = 500;
        }

        this.messageTunnel_messageMaxLength = this.getNode(this.data,"message-tunnel.message-max-length",Integer.class);
        if(this.messageTunnel_messageMaxLength < 384) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Max message length is to small to be effective! " + this.messageTunnel_messageMaxLength + " < 384. Max message length set to 384."), NamedTextColor.YELLOW);
            this.messageTunnel_messageMaxLength = 384;
        }

        this.messageTunnel_whitelist_enabled = this.getNode(this.data,"message-tunnel.whitelist.enabled",Boolean.class);
        try {
            this.messageTunnel_whitelist_addresses = (List<String>) this.getNode(this.data,"message-tunnel.whitelist.addresses",List.class);
        } catch (Exception e) {
            throw new IllegalStateException("The node [message-tunnel.whitelist] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        this.messageTunnel_denylist_enabled = this.getNode(this.data,"message-tunnel.denylist.enabled",Boolean.class);
        try {
            this.messageTunnel_denylist_addresses = (List<String>) this.getNode(this.data,"message-tunnel.denylist.addresses",List.class);
        } catch (Exception e) {
            throw new IllegalStateException("The node [message-tunnel.denylist] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

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
