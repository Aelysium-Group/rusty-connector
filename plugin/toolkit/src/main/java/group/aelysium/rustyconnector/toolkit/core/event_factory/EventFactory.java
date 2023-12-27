package group.aelysium.rustyconnector.toolkit.core.event_factory;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.ArrayList;
import java.util.List;

public class EventFactory implements Service {
    private final List<Listener<Event>> listeners = new ArrayList<>();

    /**
     * Add a listener to the factory.
     * Any time the defined event is thrown, the listener will trigger.
     * @param listener The listener to add.
     */
    public void addListener(Listener<Event> listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener<Event> listener) {
        listeners.removeIf(item -> item.equals(listener));
    }

    public void fire(Event event) {
        this.listeners.stream().filter(listener -> listener.invoker().equals(event.getClass())).toList().forEach(entry -> {
            try {
                entry.execute(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void kill() {
        this.listeners.clear();
    }
}