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

    public FamilyConfig(File configPointer, String template) {
        super(configPointer, template);
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
     * @param key The name of the whitelist config to get.
     * @param config The whitelist config to put.
     */
    public static void addConfig(String key, FamilyConfig config) {
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
        this.loadBalancing_algorithm = this.getNode(this.data,"load-balancing.algorithm",String.class);
        this.loadBalancing_persistence = this.getNode(this.data,"load-balancing.persistence",Boolean.class);

        this.use_whitelist = this.getNode(this.data,"use-whitelist",Boolean.class);
        this.whitelist = this.getNode(this.data,"whitelist",String.class);
    }
}
