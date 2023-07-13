package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FriendsConfig extends YAML {
    private static FriendsConfig config;

    private boolean enabled = false;
    private int maxFriends = 25;

    private String mysql_host = "";
    private int mysql_port = 3306;
    private String mysql_user = "root";
    private String mysql_password = "password";
    private String mysql_database = "RustyConnector";

    private boolean onlyTPAToFriends = false;

    private FriendsConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static FriendsConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static FriendsConfig newConfig(File configPointer, String template) {
        config = new FriendsConfig(configPointer, template);
        return FriendsConfig.getConfig();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxFriends() {
        return maxFriends;
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

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        this.enabled = this.getNode(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.maxFriends = this.getNode(this.data, "max-friends", Integer.class);

        // MySQL

        this.mysql_host = this.getNode(this.data, "mysql.host", String.class);
        if (this.mysql_host.equals("")) throw new IllegalStateException("Please configure your MySQL settings.");

        this.mysql_port = this.getNode(this.data, "mysql.port", Integer.class);
        this.mysql_user = this.getNode(this.data, "mysql.user", String.class);
        this.mysql_password = this.getNode(this.data, "mysql.password", String.class);

        if (this.mysql_password.length() != 0 && this.mysql_password.length() < 16)
            throw new IllegalStateException("Your MySQL password is to short! For security purposes, please use a longer password! " + this.mysql_password.length() + " < 16");

        this.mysql_database = this.getNode(this.data, "mysql.database", String.class);
        if (this.mysql_database.equals(""))
            throw new IllegalStateException("You must pass a proper name for the database to use with MySQL!");
    }
}
