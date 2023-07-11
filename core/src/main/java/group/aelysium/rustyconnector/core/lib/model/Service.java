package group.aelysium.rustyconnector.core.lib.model;

import group.aelysium.rustyconnector.core.lib.exception.DisabledServiceException;

import java.util.Map;

public abstract class Service {
    private final boolean enabled;

    public Service(boolean enabled) {
       this.enabled = enabled;
    }

    public abstract void kill();

    protected void throwIfDisabled() {
        if(!this.enabled) throw new DisabledServiceException("Attempted to access a Service which is disabled!");
    }
}
