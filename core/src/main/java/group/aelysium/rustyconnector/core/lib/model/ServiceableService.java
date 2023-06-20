package group.aelysium.rustyconnector.core.lib.model;

import group.aelysium.rustyconnector.core.lib.exception.DisabledServiceException;

import java.util.Map;

public abstract class ServiceableService extends Service {
    protected final Map<Class<? extends Service>, Service> services;

    public ServiceableService(boolean enabled, Map<Class<? extends Service>, Service> services) {
        super(enabled);
        this.services = services;
    }

    public <S extends Service> S getService(Class<S> type) {
        return (S) this.services.get(type);
    }
}
