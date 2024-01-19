package group.aelysium.rustyconnector.toolkit.core.events;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

public interface EventManager extends Service {
    /**
     * Registers a new listener to this manager.
     */
    void on(Class<? extends Event> event, Listener<?> listener);

    /**
     * Unregisters a listener from this manager.
     */
    void off(Class<? extends Event> event, Listener<?> listener);
}
