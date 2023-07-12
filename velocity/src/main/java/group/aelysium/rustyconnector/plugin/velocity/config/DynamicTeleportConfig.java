package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DynamicTeleportConfig extends YAML {
    private static DynamicTeleportConfig config;

    private boolean enabled = false;
    private boolean tpa_enabled = false;
    private boolean tpa_friendsOnly = false;
    private List<String> tpa_enabledFamilies = new ArrayList<>();
    private boolean tpa_ignorePlayerCap = false;
    private LiquidTimestamp tpa_expiration;

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

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        this.enabled = this.getNode(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.tpa_enabled = this.getNode(this.data, "tpa.enabled", Boolean.class);
        if(!this.tpa_enabled) return;


        this.tpa_friendsOnly = this.getNode(this.data, "tpa.friends-only", Boolean.class);

        try {
            this.tpa_enabledFamilies = (List<String>) (this.getNode(this.data,"tpa.enabled-families",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [tpa.enabled-families] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.tpa_ignorePlayerCap = this.getNode(this.data, "tpa.ignore-player-cap", Boolean.class);


        try {
            String expiration = this.getNode(this.data, "tpa.expiration", String.class);
            if(expiration.equals("NEVER")) this.tpa_expiration = null;
            else this.tpa_expiration = new LiquidTimestamp(expiration);
        } catch (ParseException e) {
            throw new IllegalStateException("You must provide a valid time value for [tpa.tpa_expiration] in dynamic_teleport.yml!");
        }
    }
}
