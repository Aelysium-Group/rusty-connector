package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.config.StaticFamilyConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.nio.file.Path;

public class ScalarFamilyConfig extends YAML {
    private Component displayName;
    private Family.Reference parent_family = Family.Reference.rootFamily();
    private String loadBalancer = "default";
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    protected ScalarFamilyConfig(Path dataFolder, String target, LangService lang) {
        super(dataFolder, target, lang, LangFileMappings.PROXY_SCALAR_FAMILY_TEMPLATE);
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

    protected void register() throws IllegalStateException {
        try {
            String name = this.getNode(this.data, "display-name", String.class);
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

    public static ScalarFamilyConfig construct(Path dataFolder, String familyName, LangService lang) {
        ScalarFamilyConfig config = new ScalarFamilyConfig(dataFolder, "families/"+familyName+".scalar.yml", lang);
        config.register();
        return config;
    }
}