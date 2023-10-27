package group.aelysium.rustyconnector.core.lib.serviceable;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ServiceHandler {
    protected final Map<Class<? extends Service>, Service> map;

    public ServiceHandler(Map<Class<? extends Service>, Service> services) {
        this.map = services;
    }
    public ServiceHandler() {
        this.map = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected <S extends Service> Optional<S> find(Class<S> type) {
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