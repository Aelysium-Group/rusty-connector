package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhitelistConfig extends YAML {
    private static Map<String,WhitelistConfig> configs = new HashMap<>();

    private boolean use_players = false;
    private List<Object> players = new ArrayList<>();

    private boolean use_permission = false;

    private boolean use_country = false;
    private List<String> countries = new ArrayList<>();

    private String message = "You aren't whitelisted on this server!";

    public WhitelistConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get a whitelist config.
     * @param key The name of the whitelist config to get.
     * @return A whtielist config.
     */
    public static WhitelistConfig getConfig(String key) {
        return WhitelistConfig.configs.get(key);
    }

    /**
     * Add a whitelist config to the proxy.
     * @param key The name of the whitelist config to get.
     * @param config The whitelist config to put.
     */
    public static void addConfig(String key, WhitelistConfig config) {
        configs.put(key, config);
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        configs = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.use_players = this.getNode(this.data,"use-players",Boolean.class);
        try {
            this.players = (this.getNode(this.data,"players",List.class));
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [players] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.use_permission = this.getNode(this.data,"use-permission",Boolean.class);

        this.use_country = this.getNode(this.data,"use-country",Boolean.class);
        try {
            this.countries = (List<String>) (this.getNode(this.data,"countries",List.class));
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [countries] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.message = this.getNode(data,"message",String.class);
    }
}
