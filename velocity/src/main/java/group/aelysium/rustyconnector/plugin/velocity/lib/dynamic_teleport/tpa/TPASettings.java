package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

public class TPASettings {
    private boolean enabled = false;
    private boolean ignorePlayerCap = false;
    private int requestLifetime = 5;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIgnorePlayerCap() {
        return ignorePlayerCap;
    }
    public int getRequestLifetime() { return this.requestLifetime; }

    public TPASettings(boolean enabled, boolean ignorePlayerCap, int requestLifetime) {
        this.enabled = enabled;
        this.ignorePlayerCap = ignorePlayerCap;
        this.requestLifetime = requestLifetime;
    }
}
