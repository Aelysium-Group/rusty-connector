package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultConfig extends YAML {
    private static DefaultConfig config;

    private String private_key = "";
    private String families_rootFamily_name = "lobby";
    private Boolean family_rootFamily_catchDisconnectiongPlayers = false;
    private List<String> families_scalar = new ArrayList<>();
    private List<String> families_static = new ArrayList<>();

    private String redis_host = "";
    private int redis_port = 3306;
    private String redis_user = "default";
    private String redis_password = "password";
    private String redis_dataChannel = "rustyConnector-sync";

    private boolean ignore_mysql = true;
    private String mysql_host = "";
    private int mysql_port = 3306;
    private String mysql_user = "root";
    private String mysql_password = "password";
    private String mysql_database = "RustyConnector";

    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    private int messageTunnel_messageCacheSize = 50;
    private int messageTunnel_messageMaxLength = 512;
    private boolean messageTunnel_whitelist_enabled = false;
    private List<String> messageTunnel_whitelist_addresses = new ArrayList<>();
    private boolean messageTunnel_denylist_enabled = false;
    private List<String> messageTunnel_denylist_addresses = new ArrayList<>();

    private boolean bootCommands_enabled = false;
    private List<String> bootCommands_commands = new ArrayList<>();

    private Boolean hearts_serverLifecycle_enabled = true;
    private Integer hearts_serverLifecycle_interval = 30;
    private Boolean hearts_serverLifecycle_unregisterOnIgnore = false;
    private Boolean messageTunnel_familyServerSorting_enabled = true;
    private Integer messageTunnel_familyServerSorting_interval = 20;

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

    public String getRootFamilyName() {
        return this.families_rootFamily_name;
    }
    public Boolean shouldRootFamilyCatchDisconnectingPlayers() {
        return this.family_rootFamily_catchDisconnectiongPlayers;
    }

    public List<String> getScalarFamilies() {
        return this.families_scalar;
    }
    public List<String> getStaticFamilies() {
        return this.families_static;
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

    public boolean shouldIgnoreMysql() {
        return this.ignore_mysql;
    }

    public String getMysql_host() {
        return this.mysql_host;
    }
    public int getMysql_port() {
        return this.mysql_port;
    }
    public String getMysql_password() {
        return this.mysql_password;
    }
    public String getMysql_user() {
        return this.mysql_user;
    }
    public String getMysql_database() {
        return this.mysql_database;
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

    public boolean isBootCommands_enabled() {
        return bootCommands_enabled;
    }

    public List<String> getBootCommands_commands() {
        return bootCommands_commands;
    }

    public Boolean isHearts_serverLifecycle_enabled() {
        return hearts_serverLifecycle_enabled;
    }

    public Integer getHearts_serverLifecycle_interval() {
        return hearts_serverLifecycle_interval;
    }

    public Boolean shouldHearts_serverLifecycle_unregisterOnIgnore() {
        return hearts_serverLifecycle_unregisterOnIgnore;
    }

    public Integer getMessageTunnel_familyServerSorting_interval() {
        return messageTunnel_familyServerSorting_interval;
    }

    public Boolean getMessageTunnel_familyServerSorting_enabled() {
        return messageTunnel_familyServerSorting_enabled;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        try {
            this.processVersion();
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        try {
            this.private_key = this.getNode(this.data,"private-key",String.class);
            if(this.private_key.equals("")) throw new Exception("You must provide a private key!");
        } catch (Exception e) {
            VelocityLang.PRIVATE_KEY.send(logger);
            throw new NoOutputException(e);
        }

        // Families
        this.families_rootFamily_name = this.getNode(this.data,"families.root-family.name",String.class);
        this.family_rootFamily_catchDisconnectiongPlayers = this.getNode(this.data,"families.root-family.catch-disconnecting-players",Boolean.class);
        try {
            this.families_scalar = (List<String>) (this.getNode(this.data,"families.scalar",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [families.scalar] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            this.families_static = (List<String>) (this.getNode(this.data,"families.static",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [families.scalar] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        if(this.families_rootFamily_name.equalsIgnoreCase("all")) throw new IllegalStateException("You can't name a family: `all`");
        this.families_scalar.forEach(familyName -> {
            if(familyName.equalsIgnoreCase("all")) throw new IllegalStateException("You can't name a family: `all`");

            if(familyName.length() > 32)
                throw new IllegalStateException("All family names must be under 32 characters long! `" + familyName + "` was " + familyName.length());
        });

        AtomicBoolean ignoreStatic = new AtomicBoolean(false);
        this.families_static.forEach(familyName -> {
            if(familyName.equalsIgnoreCase("delete me to enable static families (Requires MySQL to be setup)"))
                ignoreStatic.set(true);

            if(familyName.equalsIgnoreCase("all")) throw new IllegalStateException("You can't name a family: `all`");

            if(familyName.length() > 32)
                throw new IllegalStateException("All family names must be under 32 characters long! `" + familyName + "` was " + familyName.length());
        });
        if(ignoreStatic.get())
            this.families_static.clear(); // Clear static family list so that nothing can be operated on it.

        List<String> duplicates = this.families_scalar.stream().filter(this.families_static::contains).toList();
        if(duplicates.size() > 0)
            throw new IllegalStateException("You can't have two families with the same name! This rule is regardless of if the family is scalar or static! Duplicate family names: " + duplicates);

        if(this.families_scalar.contains(this.families_rootFamily_name)) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text(this.families_rootFamily_name + " was found included in [families.scalar] in config.yml. This is no longer supported. Instead, ONLY place the name of your root family in [families.root-family]. Ignoring..."), NamedTextColor.YELLOW);
            this.families_scalar.remove(this.families_rootFamily_name);
        }
        if(this.families_static.contains(this.families_rootFamily_name)) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text(this.families_rootFamily_name + " was found included in [families.static] in config.yml. This is no longer supported. Instead, ONLY place the name of your root family in [families.root-family]. Ignoring..."), NamedTextColor.YELLOW);
            this.families_static.remove(this.families_rootFamily_name);
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

        // MySQL

        if(!ignoreStatic.get()) {
            this.ignore_mysql = false;

            this.mysql_host = this.getNode(this.data, "mysql.host", String.class);
            if (this.mysql_host.equals("")) throw new IllegalStateException("Please configure your MySQL settings.");

            this.mysql_port = this.getNode(this.data, "mysql.port", Integer.class);
            this.mysql_user = this.getNode(this.data, "mysql.user", String.class);
            this.mysql_password = this.getNode(this.data, "mysql.password", String.class);

            if (this.redis_password.length() != 0 && this.redis_password.length() < 16)
                throw new IllegalStateException("Your MySQL password is to short! For security purposes, please use a longer password! " + this.redis_password.length() + " < 16");

            this.mysql_database = this.getNode(this.data, "mysql.database", String.class);
            if (this.mysql_database.equals(""))
                throw new IllegalStateException("You must pass a proper name for the database to use with MySQL!");
        }

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

        // Boot commands

        this.bootCommands_enabled = this.getNode(this.data,"boot-commands.enabled",Boolean.class);
        try {
            this.bootCommands_commands = (List<String>) this.getNode(this.data,"boot-commands.commands",List.class);
        } catch (Exception e) {
            throw new IllegalStateException("The node [boot-commands.commands] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        // Hearts
        this.hearts_serverLifecycle_enabled = this.getNode(this.data,"hearts.server-lifecycle.enabled",Boolean.class);
        this.hearts_serverLifecycle_interval = this.getNode(this.data,"hearts.server-lifecycle.interval",Integer.class);
        if(this.hearts_serverLifecycle_interval < 10) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Server lifecycle interval is set dangerously fast: " + this.hearts_serverLifecycle_interval + "ms. Setting to default of 30ms."), NamedTextColor.YELLOW);
            this.messageTunnel_messageMaxLength = 30;
        }
        this.hearts_serverLifecycle_unregisterOnIgnore = this.getNode(this.data,"hearts.server-lifecycle.unregister-on-ignore",Boolean.class);

        this.messageTunnel_familyServerSorting_enabled = this.getNode(this.data,"hearts.family-server-sorting.enabled",Boolean.class);
        this.messageTunnel_familyServerSorting_interval = this.getNode(this.data,"hearts.family-server-sorting.interval",Integer.class);
        if(this.messageTunnel_familyServerSorting_interval < 7) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Server sorting interval is set dangerously fast: " + this.messageTunnel_familyServerSorting_interval + "ms. Setting to default of 20ms."), NamedTextColor.YELLOW);
            this.messageTunnel_familyServerSorting_interval = 20;
        }
    }
}
