package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;

import java.io.File;

public class ScalarFamilyConfig extends YAML {
    private String parent_family = "";
    private String loadBalancer = "default";
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    public ScalarFamilyConfig(String dataFolder, String familyName) {
        super(new File(dataFolder, "families/"+familyName+".scalar.yml"));
    }
    public String getParent_family() { return parent_family; }
    public String loadBalancer() { return loadBalancer; }

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

        try {
            this.loadBalancer = this.getNode(this.data, "load-balancer", String.class);
        } catch (Exception ignore) {
            this.loadBalancer = "default";
        }
        this.loadBalancer = this.loadBalancer.replaceFirst("\\.yml$|\\.yaml$","");

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.name cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }
}