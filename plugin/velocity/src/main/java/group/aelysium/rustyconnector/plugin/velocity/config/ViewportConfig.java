package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.SyncedRole;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.SyncedUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ViewportConfig extends YAML {
    private static ViewportConfig config;

    private boolean enabled = false;

    private String mysql_host = "";
    private int mysql_port = 3306;
    private String mysql_user = "root";
    private String mysql_password = "password";
    private String mysql_database = "RustyConnector";

    private InetSocketAddress api_address;
    private InetSocketAddress public_address;
    private LiquidTimestamp public_afkExpiration;

    private List<SyncedRole> roles = new ArrayList<>();
    private List<SyncedUser> users = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }
    public String getMysql_host() {
        return mysql_host;
    }
    public int getMysql_port() {
        return mysql_port;
    }
    public String getMysql_user() {
        return mysql_user;
    }
    public String getMysql_password() {
        return mysql_password;
    }
    public String getMysql_database() {
        return mysql_database;
    }
    public InetSocketAddress getApi_address() {
        return api_address;
    }
    public InetSocketAddress getPublic_address() {
        return public_address;
    }
    public LiquidTimestamp getPublic_afkExpiration() {
        return public_afkExpiration;
    }
    public List<SyncedRole> getRoles() {
        return roles;
    }
    public List<SyncedUser> getUsers() {
        return users;
    }

    private ViewportConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static ViewportConfig newConfig(File configPointer, String template) {
        config = new ViewportConfig(configPointer, template);
        return ViewportConfig.config();
    }

    private static ViewportConfig config() {
        return config;
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static ViewportConfig getConfig() {
        return config;
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.enabled = this.getNode(this.data, "enabled", Boolean.class);

        this.api_address = AddressUtil.parseAddress(
                this.getNode(this.data, "api.hostname", String.class) + ":" +
                        this.getNode(this.data, "api.port", Integer.class)
        );

        this.public_address = AddressUtil.parseAddress(
                this.getNode(this.data, "public.hostname", String.class) + ":" +
                        this.getNode(this.data, "public.port", Integer.class)
        );
        try {
            LiquidTimestamp expiration = LiquidTimestamp.from(this.getNode(this.data, "public.afk-expiration", String.class));
            if (expiration.compareTo(new LiquidTimestamp(30, TimeUnit.MINUTES)) < 0) {
                this.public_afkExpiration = new LiquidTimestamp(30, TimeUnit.MINUTES);
                VelocityAPI.get().logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("[public.afk-expiration] must be at least 30 Minutes."), NamedTextColor.YELLOW));
            } else this.public_afkExpiration = expiration;
        } catch (ParseException e) {
            throw new IllegalStateException("You must provide a valid time value for [public.afk-expiration] in viewport.yml!");
        }

        this.mysql_host = this.getNode(this.data, "mysql.host", String.class);
        if (this.mysql_host.equals("")) throw new IllegalStateException("Please configure your MySQL settings.");

        this.mysql_port = this.getNode(this.data, "mysql.port", Integer.class);
        this.mysql_user = this.getNode(this.data, "mysql.user", String.class);
        this.mysql_password = this.getNode(this.data, "mysql.password", String.class);
        this.mysql_database = this.getNode(this.data, "mysql.database", String.class);
        if (this.mysql_database.equals(""))
            throw new IllegalStateException("You must pass a proper name for the database to use with MySQL!");
    }
}
