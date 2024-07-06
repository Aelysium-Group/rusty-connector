package group.aelysium.rustyconnector.toolkit.common.events;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class EventManager implements Particle {

    // A map of event types to their listeners
    private final Map<Class<? extends Event>, Vector<Listener<Event>>> listeners = new ConcurrentHashMap<>();

    // A ForkJoinPool to execute the events asynchronously
    private final ExecutorService executor = ForkJoinPool.commonPool();

    // Constructor
    public EventManager() {}

    /**
     * Registers a new listener to this manager.
     */
    public void on(Class<? extends Event> event, Listener<?> listener) {
        listeners.computeIfAbsent(event, k -> new Vector<>()).add((Listener<Event>) listener);
    }

    /**
     * Unregisters a listener from this manager.
     */
    public void off(Class<? extends Event> event, Listener<?> listener) {
        Vector<Listener<Event>> listeners = this.listeners.get(event);
        if(listeners == null) return;
        listeners.remove(listener);
    }

    /**
     * Fires the event.
     */
    public void fireEvent(Event event) {
        // Get the listener for the event type
        Vector<Listener<Event>> listeners = this.listeners.get(event.getClass());

        // If the listener exists, submit a task to the executor
        if (listeners == null) return;

        executor.execute(() -> {
            ((Vector<Listener<Event>>) listeners.clone()).forEach(listener -> listener.handler(event));
        });
    }


    public void close() throws Exception {
        this.listeners.clear();
        this.executor.shutdown();
    }
}
