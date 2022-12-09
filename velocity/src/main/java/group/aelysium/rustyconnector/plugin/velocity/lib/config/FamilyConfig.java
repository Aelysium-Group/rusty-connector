package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyConfig extends YAML {
    private static Map<String, FamilyConfig> configs = new HashMap<>();

    private String loadBalancing_algorithm = "ROUND_ROBIN";
    private boolean loadBalancing_persistence = false;
    private boolean use_whitelist = false;
    private String whitelist = "whitelist-template";

    private FamilyConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public static Map<String, FamilyConfig> getConfigs() {
        return configs;
    }

    public String getLoadBalancing_algorithm() {
        return loadBalancing_algorithm;
    }

    public boolean getLoadBalancing_persistence() {
        return loadBalancing_persistence;
    }

    public boolean getUse_whitelist() {
        return use_whitelist;
    }

    public String getWhitelist() {
        return whitelist;
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
        this.loadBalancing_algorithm = this.getNode(this.data,"load-balancing.algorithm",String.class);
        this.loadBalancing_persistence = this.getNode(this.data,"load-balancing.persistence",Boolean.class);

        this.use_whitelist = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist = this.getNode(this.data,"whitelist.name",String.class);
    }
}
