package group.aelysium.rustyconnector.core.lib.model;

import java.util.HashMap;
import java.util.Map;

public abstract class Serviceable {
    protected final Map<Class<? extends Service>, Service> services;

    public Serviceable(Map<Class<? extends Service>, Service> services) {
        this.services = services;
    }

    public <S extends Service> S getService(Class<S> type) {
        return (S) this.services.get(type);
    }
}
