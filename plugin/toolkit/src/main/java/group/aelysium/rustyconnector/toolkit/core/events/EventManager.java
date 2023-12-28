package group.aelysium.rustyconnector.toolkit.core.events;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import net.engio.mbassy.listener.Listener;

public interface EventManager extends Service {
    /**
     * Registers a new listener to this manager.
     * @param listener {@link Listener}
     */
    void on(Object listener);

    /**
     * Unregisters a listener from this manager.
     * @param listener {@link Listener}
     */
    void off(Object listener);
}
