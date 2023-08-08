package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.UnavailableProtocol;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class StaticFamilyConfig extends YAML {
    private static Map<String, StaticFamilyConfig> configs = new HashMap<>();
    private String parent_family = "";
    private boolean firstConnection_loadBalancing_weighted = false;
    private String firstConnection_loadBalancing_algorithm = "ROUND_ROBIN";
    private boolean firstConnection_loadBalancing_persistence_enabled = false;
    private int firstConnection_loadBalancing_persistence_attempts = 5;

    private UnavailableProtocol consecutiveConnections_homeServer_ifUnavailable = UnavailableProtocol.ASSIGN_NEW_HOME;
    private LiquidTimestamp consecutiveConnections_homeServer_expiration = null;
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    private StaticFamilyConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public String getParent_family() { return parent_family; }

    public boolean isFirstConnection_loadBalancing_weighted() {
        return firstConnection_loadBalancing_weighted;
    }

    public boolean isFirstConnection_loadBalancing_persistence_enabled() {
        return firstConnection_loadBalancing_persistence_enabled;
    }

    public int getFirstConnection_loadBalancing_persistence_attempts() {
        return firstConnection_loadBalancing_persistence_attempts;
    }

    public String getFirstConnection_loadBalancing_algorithm() {
        return firstConnection_loadBalancing_algorithm;
    }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }
    public UnavailableProtocol getConsecutiveConnections_homeServer_ifUnavailable() {
        return consecutiveConnections_homeServer_ifUnavailable;
    }
    public LiquidTimestamp getConsecutiveConnections_homeServer_expiration() {
        return consecutiveConnections_homeServer_expiration;
    }

    /**
     * Add a whitelist config to the proxy.
     * @param name The name of the whitelist family to save.
     * @param configPointer The config file.
     * @param template The path to the template config file.
     */
    public static StaticFamilyConfig newConfig(String name, File configPointer, String template) {
        StaticFamilyConfig config = new StaticFamilyConfig(configPointer, template);
        configs.put(name, config);
        return config;
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        configs = new HashMap<>();
    }

    public void register() throws IllegalStateException {
        this.parent_family = this.getNode(this.data, "parent-family", String.class);

        this.firstConnection_loadBalancing_weighted = this.getNode(this.data,"first-connection.load-balancing.weighted",Boolean.class);
        this.firstConnection_loadBalancing_algorithm = this.getNode(this.data,"first-connection.load-balancing.algorithm",String.class);

        try {
            Enum.valueOf(AlgorithmType.class, this.firstConnection_loadBalancing_algorithm);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("The load balancing algorithm: "+this.firstConnection_loadBalancing_algorithm +" doesn't exist!");
        }

        this.firstConnection_loadBalancing_persistence_enabled = this.getNode(this.data,"first-connection.load-balancing.persistence.enabled",Boolean.class);
        this.firstConnection_loadBalancing_persistence_attempts = this.getNode(this.data,"first-connection.load-balancing.persistence.attempts",Integer.class);
        if(this.firstConnection_loadBalancing_persistence_enabled && this.firstConnection_loadBalancing_persistence_attempts <= 0)
            throw new IllegalStateException("Load balancing persistence must allow at least 1 attempt.");

        try {
            this.consecutiveConnections_homeServer_ifUnavailable = UnavailableProtocol.valueOf(this.getNode(this.data,"consecutive-connections.home-server.if-unavailable",String.class));
        } catch (IllegalArgumentException ignore) {
            throw new IllegalStateException("You must provide a valid option for [consecutive-connections.home-server.if-unavailable] in your static family configs!");
        }
        try {
            String expiration = this.getNode(this.data, "consecutive-connections.home-server.expiration", String.class);
            if(expiration.equals("NEVER")) this.consecutiveConnections_homeServer_expiration = null;
            else this.consecutiveConnections_homeServer_expiration = new LiquidTimestamp(expiration);
        } catch (ParseException e) {
            throw new IllegalStateException("You must provide a valid time value for [consecutive-connections.home-server.expiration] in your static family configs!");
        }

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.name cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }
}
