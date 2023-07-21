package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DynamicTeleportConfig extends YAML {
    private static DynamicTeleportConfig config;

    private boolean enabled = false;
    private boolean tpa_enabled = false;
    private boolean tpa_friendsOnly = false;
    private List<String> tpa_enabledFamilies = new ArrayList<>();
    private boolean tpa_ignorePlayerCap = false;
    private LiquidTimestamp tpa_expiration;

    private boolean familyAnchor_enabled = false;

    private List<Map.Entry<String, String>> familyAnchor_anchors;

    private boolean hub_enabled = false;
    private List<String> hub_enabledFamilies = new ArrayList<>();

    private DynamicTeleportConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static DynamicTeleportConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static DynamicTeleportConfig newConfig(File configPointer, String template) {
        config = new DynamicTeleportConfig(configPointer, template);
        return DynamicTeleportConfig.getConfig();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isTpa_enabled() {
        return tpa_enabled;
    }

    public boolean isTpa_friendsOnly() {
        return tpa_friendsOnly;
    }

    public List<String> getTpa_enabledFamilies() {
        return tpa_enabledFamilies;
    }

    public boolean isTpa_ignorePlayerCap() {
        return tpa_ignorePlayerCap;
    }

    public LiquidTimestamp getTpa_expiration() {
        return tpa_expiration;
    }

    public boolean isFamilyAnchor_enabled() {
        return familyAnchor_enabled;
    }

    public List<Map.Entry<String, String>> getFamilyAnchor_anchors() {
        return familyAnchor_anchors;
    }

    public boolean isHub_enabled() {
        return hub_enabled;
    }

    public List<String> getHub_enabledFamilies() {
        return hub_enabledFamilies;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        this.enabled = this.getNode(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.tpa_enabled = this.getNode(this.data, "tpa.enabled", Boolean.class);
        if(this.tpa_enabled) {
            this.tpa_friendsOnly = this.getNode(this.data, "tpa.friends-only", Boolean.class);

            try {
                this.tpa_enabledFamilies = (List<String>) (this.getNode(this.data, "tpa.enabled-families", List.class));
            } catch (Exception e) {
                throw new IllegalStateException("The node [tpa.enabled-families] in " + this.getName() + " is invalid! Make sure you are using the correct type of data!");
            }

            this.tpa_ignorePlayerCap = this.getNode(this.data, "tpa.ignore-player-cap", Boolean.class);


            try {
                String expiration = this.getNode(this.data, "tpa.expiration", String.class);
                if (expiration.equals("NEVER")) {
                    this.tpa_expiration = new LiquidTimestamp(5, TimeUnit.MINUTES);
                    VelocityAPI.get().logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("\"NEVER\" as a Liquid Timestamp for [tpa.expiration] is not allowed! Set to default of 5 Minutes."), NamedTextColor.YELLOW));
                } else this.tpa_expiration = new LiquidTimestamp(expiration);
            } catch (ParseException e) {
                throw new IllegalStateException("You must provide a valid time value for [tpa.expiration] in dynamic_teleport.yml!");
            }
        }

        this.familyAnchor_enabled = this.getNode(this.data, "family-anchor.enabled", Boolean.class);
        if(this.familyAnchor_enabled) {
            List<? extends ConfigurationNode> anchors = get(this.data, "family-anchor.anchors").getChildrenList();

            this.familyAnchor_anchors = new ArrayList<>();
            if(anchors.size() != 0)
                for (ConfigurationNode entry: anchors)
                    this.familyAnchor_anchors.add(Map.entry(
                            this.getNode(entry, "name", String.class),
                            this.getNode(entry, "family", String.class)
                    ));
        }

        this.hub_enabled = this.getNode(this.data, "hub.enabled", Boolean.class);
        if(this.hub_enabled) {
            try {
                this.hub_enabledFamilies = (List<String>) (this.getNode(this.data, "hub.enabled-families", List.class));
            } catch (Exception e) {
                throw new IllegalStateException("The node [hub.enabled-families] in " + this.getName() + " is invalid! Make sure you are using the correct type of data!");
            }
        }
    }
}
