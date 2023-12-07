package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RankedFamilyConfig extends YAML {
    private Component displayName;
    private Family.Reference parent_family = Family.Reference.rootFamily();
    private String matchmaker;
    private String gamemodeName;
    private boolean whitelist_enabled;
    private String whitelist_name;

    public RankedFamilyConfig(String dataFolder, String familyName) {
        super(new File(dataFolder, "families/"+familyName+".ranked.yml"));
    }

    public Component displayName() { return displayName; }
    public Family.Reference getParent_family() { return parent_family; }

    public String gamemodeName() {
        return gamemodeName;
    }
    public String matchmaker() {
        return matchmaker;
    }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    public void register(String familyName) throws IllegalStateException {
        try {
            String name = this.getNode(this.data, "display-id", String.class);
            this.displayName = MiniMessage.miniMessage().deserialize(name);
        } catch (Exception ignore) {}

        try {
            this.parent_family = new Family.Reference(this.getNode(this.data, "parent-family", String.class));
        } catch (Exception ignore) {}

        this.gamemodeName = this.getNode(this.data,"gamemode-name",String.class);
        if(this.gamemodeName.equalsIgnoreCase("default") || this.gamemodeName.equalsIgnoreCase(""))
            this.gamemodeName = familyName;

        this.matchmaker = this.getNode(this.data,"matchmaker",String.class);

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.id",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }
}