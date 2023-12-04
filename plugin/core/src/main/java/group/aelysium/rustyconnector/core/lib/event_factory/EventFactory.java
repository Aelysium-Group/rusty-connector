package group.aelysium.rustyconnector.core.lib.event_factory;

import group.aelysium.rustyconnector.toolkit.velocity.util.Entry;

import java.util.ArrayList;
import java.util.List;

public class EventFactory {
    private final List<Entry<Listener, Class<? extends Event>>> listeners = new ArrayList<>();

    /**
     * Add a listener to the factory.
     * Any time the defined event is thrown, the listener will trigger.
     * @param listener The listener to add.
     */
    public void addListener(Listener listener) {
        listeners.add(new Entry<>(listener, listener.invoker()));
    }

    public void removeListener(Listener listener) {
        listeners.removeIf(item -> item.getKey().equals(listener));
    }

    public void fire(Event event) {
        this.listeners.stream().filter(listener -> listener.getKey().invoker() == event.getClass()).toList().forEach(entry -> {
            Listener listener = entry.getKey();
            listener.execute(event);
        });
    }
}