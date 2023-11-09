package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;

import java.io.File;

public class ScalarFamilyConfig extends YAML {
    private String parent_family = "";
    private boolean loadBalancing_weighted = false;
    private String loadBalancing_algorithm = "ROUND_ROBIN";
    private boolean loadBalancing_persistence_enabled = false;
    private int loadBalancing_persistence_attempts = 5;
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    public ScalarFamilyConfig(File configPointer) {
        super(configPointer);
    }

    public String getParent_family() { return parent_family; }

    public boolean isLoadBalancing_weighted() {
        return loadBalancing_weighted;
    }

    public boolean isLoadBalancing_persistence_enabled() {
        return loadBalancing_persistence_enabled;
    }

    public int getLoadBalancing_persistence_attempts() {
        return loadBalancing_persistence_attempts;
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

    public void register() throws IllegalStateException {
        try {
            this.parent_family = this.getNode(this.data, "parent-family", String.class);
        } catch (Exception ignore) {
            this.parent_family = "";
        }

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