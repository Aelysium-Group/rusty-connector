package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultConfig extends YAML {
    private static DefaultConfig config;

    private String private_key = "";
    private String public_key = "";
    private int heartbeat = 10;
    private String root_family = "lobby";
    private List<String> families = new ArrayList<>();

    private String redis_host = "localhost";
    private int redis_port = 3306;
    private String redis_password = "password";
    private String redis_dataChannel = "rustyConnector-sync";

    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    private boolean messageTunnel_enabled = false;
    private List<String> messageTunnel_whitelist = new ArrayList<>();
    private List<String> messageTunnel_denylist = new ArrayList<>();

    private boolean bootCommands_enabled = false;
    private List<String> bootCommands_commands = new ArrayList<>();

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

    public String getPrivate_key() {
        return this.private_key;
    }

    public String getPublic_key() {
        return this.public_key;
    }

    public int getHeartbeat() {
        return this.heartbeat;
    }

    public String getRoot_family() {
        return this.root_family;
    }

    public List<String> getFamilies() {
        return this.families;
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

    public String getRedis_dataChannel() {
        return this.redis_dataChannel;
    }

    public boolean isWhitelist_enabled() {
        return this.whitelist_enabled;
    }

    public String getWhitelist_name() {
        return this.whitelist_name;
    }

    public boolean isMessageTunnel_enabled() {
        return this.messageTunnel_enabled;
    }

    public List<String> getMessageTunnel_whitelist() {
        return this.messageTunnel_whitelist;
    }

    public List<String> getMessageTunnel_denylist() {
        return this.messageTunnel_denylist;
    }

    public boolean isBootCommands_enabled() {
        return bootCommands_enabled;
    }

    public List<String> getBootCommands_commands() {
        return bootCommands_commands;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        try {
            this.private_key = this.getNode(this.data,"private-key",String.class);
        } catch (Exception e) {
            VelocityLang.PRIVATE_KEY.send(plugin.logger());
        }
        this.public_key = this.getNode(this.data,"public-key",String.class);
        this.heartbeat = this.getNode(this.data,"heart-beat",Integer.class);

        this.root_family = this.getNode(this.data,"root-family",String.class);
        try {
            this.families = (List<String>) (this.getNode(this.data,"families",List.class));
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [families] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.redis_host = this.getNode(this.data,"redis.host",String.class);
        this.redis_port = this.getNode(this.data,"redis.port",Integer.class);
        this.redis_password = this.getNode(this.data,"redis.password",String.class);
        this.redis_dataChannel = this.getNode(this.data,"redis.data-channel",String.class);

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.name",String.class);

        this.messageTunnel_enabled = this.getNode(this.data,"message-tunnel.enabled",Boolean.class);
        try {
            this.messageTunnel_whitelist = (List<String>) this.getNode(this.data,"message-tunnel.whitelist",List.class);
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [message-tunnel.whitelist] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            this.messageTunnel_denylist = (List<String>) this.getNode(this.data,"message-tunnel.denylist",List.class);
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [message-tunnel.denylist] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }


        this.bootCommands_enabled = this.getNode(this.data,"boot-commands.enabled",Boolean.class);
        try {
            this.bootCommands_commands = (List<String>) this.getNode(this.data,"boot-commands.commands",List.class);
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [boot-commands.commands] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
    }
}
