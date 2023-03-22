package group.aelysium.rustyconnector.plugin.velocity.lib.tpa;

public class TPASettings {
    private boolean enabled = false;
    private boolean ignorePlayerCap = false;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIgnorePlayerCap() {
        return ignorePlayerCap;
    }

    public TPASettings(boolean enabled, boolean ignorePlayerCap) {
        this.enabled = enabled;
        this.ignorePlayerCap = ignorePlayerCap;
    }
}
