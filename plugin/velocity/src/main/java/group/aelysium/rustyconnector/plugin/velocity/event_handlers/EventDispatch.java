package group.aelysium.rustyconnector.plugin.velocity.event_handlers;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.events.Event;

/**
 * Convenience class for fetching the event manager and firing an event.
 */
public class EventDispatch {
    /**
     * An unsafe event dispatch.
     * Methods defined here are not unsafe in that they can't be used in code, instead, they may produce exceptions when attempting to fire an event.
     */
    public static class UnSafe {
        public static void fire(Event event) {
            Tinder.get().services().events().fireEvent(event);
        }
        public static void fireAndForget(Event event) {
            Tinder.get().services().events().fireEvent(event);
        }
    }
}
