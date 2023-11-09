package group.aelysium.rustyconnector.toolkit.core.serviceable;


import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ServiceHandler implements IServiceHandler {
    protected final Map<Class<? extends Service>, Service> map;

    public ServiceHandler(Map<Class<? extends Service>, Service> services) {
        this.map = services;
    }
    public ServiceHandler() {
        this.map = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <S extends Service> Optional<S> find(Class<S> type) {
        try {
            S s = (S) this.map.get(type);
            if (s == null) return Optional.empty();
            return Optional.of(s);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public <S extends Service> void add(S service) {
        this.map.put(service.getClass(), service);
    }

    public void killAll() {
        this.map.forEach((key, value) -> value.kill());
        this.map.clear();
    }
}