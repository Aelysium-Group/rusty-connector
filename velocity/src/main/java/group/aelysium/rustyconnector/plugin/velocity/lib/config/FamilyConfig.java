package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FamilyConfig extends YAML {
    private static Map<String, FamilyConfig> configs = new HashMap<>();

    private boolean loadBalancing_weighted = false;
    private String loadBalancing_algorithm = "ROUND_ROBIN";
    private boolean loadBalancing_persistence_enabled = false;
    private int loadBalancing_persistence_attempts = 5;
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    private FamilyConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public boolean isLoadBalancing_weighted() {
        return loadBalancing_weighted;
    }

    public boolean isLoadBalancing_persistence_enabled() {
        return loadBalancing_persistence_enabled;
    }

    public int getLoadBalancing_persistence_attempts() {
        return loadBalancing_persistence_attempts;
    }
    public static Map<String, FamilyConfig> getConfigs() {
        return configs;
    }

    public String getLoadBalancing_algorithm() {
        return loadBalancing_algorithm;
    }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    /**
     * Get a whitelist config.
     * @param key The name of the whitelist config to get.
     * @return A whtielist config.
     */
    public static FamilyConfig getConfig(String key) {
        return FamilyConfig.configs.get(key);
    }

    /**
     * Add a whitelist config to the proxy.
     * @param name The name of the whitelist family to save.
     * @param configPointer The config file.
     * @param template The path to the template config file.
     */
    public static FamilyConfig newConfig(String name, File configPointer, String template) {
        FamilyConfig config = new FamilyConfig(configPointer, template);
        configs.put(name, config);
        return config;
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        configs = new HashMap<>();
    }


    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.loadBalancing_weighted = this.getNode(this.data,"load-balancing.weighted",Boolean.class);
        this.loadBalancing_algorithm = this.getNode(this.data,"load-balancing.algorithm",String.class);

        try {
            Enum.valueOf(AlgorithmType.class, this.loadBalancing_algorithm);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("The load balancing algorithm: "+this.loadBalancing_algorithm+" doesn't exist!");
        }

        this.loadBalancing_persistence_enabled = this.getNode(this.data,"load-balancing.persistence.enabled",Boolean.class);
        this.loadBalancing_persistence_attempts = this.getNode(this.data,"load-balancing.persistence.attempts",Integer.class);
        if(this.loadBalancing_persistence_enabled && this.loadBalancing_persistence_attempts <= 0)
            throw new IllegalStateException("Load balancing persistence must allow at least 1 attempt.");

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.name cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }
}
