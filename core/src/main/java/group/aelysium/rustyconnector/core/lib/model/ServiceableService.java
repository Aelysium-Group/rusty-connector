package group.aelysium.rustyconnector.core.lib.model;

import group.aelysium.rustyconnector.core.lib.exception.DisabledServiceException;

import java.util.Map;
import java.util.Optional;

public abstract class ServiceableService extends Service {
    protected final Map<Class<? extends Service>, Service> services;

    public ServiceableService(Map<Class<? extends Service>, Service> services) {
        this.services = services;
    }

    @SuppressWarnings("unchecked")
    public <S extends Service> Optional<S> getService(Class<S> type) {
        try {
            S s = (S) this.services.get(type);
            if (s == null) return Optional.empty();
            return Optional.of(s);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <S extends Service> boolean isEnabled(Class<S> type) {
        try {
            return this.services.get(type) != null;
        } catch (Exception ignore) {}
        return false;
    }

    @Override
    public void kill() {
        this.services.values().forEach(Service::kill);
        this.services.clear();
    }
}
