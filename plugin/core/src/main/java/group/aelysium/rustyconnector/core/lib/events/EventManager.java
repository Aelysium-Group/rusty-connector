package group.aelysium.rustyconnector.core.lib.events;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class EventManager implements group.aelysium.rustyconnector.toolkit.core.events.EventManager {

    // A map of event types to their listeners
    private final Map<Class<? extends Event>, Vector<Listener<Event>>> listeners = new ConcurrentHashMap<>();

    // A ForkJoinPool to execute the events asynchronously
    private final ExecutorService executor = ForkJoinPool.commonPool();

    // Constructor
    public EventManager() {}

    // Register a listener for a given event type
    public void on(Class<? extends Event> event, Listener<?> listener) {
        listeners.computeIfAbsent(event, k -> new Vector<>()).add((Listener<Event>) listener);
    }

    // Unregister a listener for a given event type
    public void off(Class<? extends Event> event, Listener<?> listener) {
        Vector<Listener<Event>> listeners = this.listeners.get(event);
        if(listeners == null) return;
        listeners.remove(listener);
    }

    // Fire an event using the fire-and-forget method
    public void fireEvent(Event event) {
        // Get the listener for the event type
        Vector<Listener<Event>> listeners = this.listeners.get(event.getClass());

        // If the listener exists, submit a task to the executor
        if (listeners == null) return;

        executor.execute(() -> {
            ((Vector<Listener<Event>>) listeners.clone()).forEach(listener -> listener.handler(event));
        });
    }

    @Override
    public void kill() {}
}
