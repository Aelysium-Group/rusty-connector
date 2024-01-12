package group.aelysium.rustyconnector.toolkit.core.serviceable;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.Vector;
import java.util.function.Consumer;

public class DelayedService<TService extends Service> implements Service {
    private TService service;
    private final Vector<Consumer<TService>> onStart = new Vector<>();
    private final Vector<Runnable> onStop = new Vector<>();

    public void onStart(Consumer<TService> consumer) {
        if(this.service != null) consumer.accept(this.service);
        this.onStart.add(consumer);
    }
    public void onStop(Runnable runnable) {
        this.onStop.add(runnable);
    }

    public void triggerStart(TService service) {
        this.service = service;
        this.onStart.forEach(consumer -> {
            try {
                consumer.accept(service);
            } catch (Exception ignore) {}
        });
    }

    public void triggerStop() {
        this.service = null;
        this.onStop.forEach(Runnable::run);
    }

    @Override
    public void kill() {
        this.service = null;
    }
}
