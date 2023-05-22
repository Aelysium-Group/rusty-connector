package group.aelysium.rustyconnector.core.lib.model;

import group.aelysium.rustyconnector.core.lib.exception.DisabledServiceException;

public class Service {
    private final boolean enabled;

    public Service(boolean enabled) {
       this.enabled = enabled;
    }

    protected void throwIfDisabled() {
        if(!this.enabled) throw new DisabledServiceException("Attempted to access a Service which is disabled!");
    }
}
