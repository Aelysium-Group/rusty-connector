package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;

public class ScalarFamilyConfig extends YAML {
    private Component displayName;
    private Family.Reference parent_family = Family.Reference.rootFamily();
    private String loadBalancer = "default";
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    public ScalarFamilyConfig(String dataFolder, String familyName) {
        super(new File(dataFolder, "families/"+familyName+".scalar.yml"));
    }
    public Component displayName() { return displayName; }
    public Family.Reference getParent_family() { return parent_family; }
    public String loadBalancer() { return loadBalancer; }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    public void register() throws IllegalStateException {
        try {
            String name = this.getNode(this.data, "display-id", String.class);
            this.displayName = MiniMessage.miniMessage().deserialize(name);
        } catch (Exception ignore) {}

        try {
            this.parent_family = new Family.Reference(this.getNode(this.data, "parent-family", String.class));
        } catch (Exception ignore) {}

        try {
            this.loadBalancer = this.getNode(this.data, "load-balancer", String.class);
        } catch (Exception ignore) {
            this.loadBalancer = "default";
        }
        this.loadBalancer = this.loadBalancer.replaceFirst("\\.yml$|\\.yaml$","");

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.id",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }
}