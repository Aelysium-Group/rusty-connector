package group.aelysium.rustyconnector.core.lib.model;

import java.util.*;

public abstract class Serviceable {
    protected final Map<Class<? extends Service>, Service> services;

    public Serviceable(Map<Class<? extends Service>, Service> services) {
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
}
