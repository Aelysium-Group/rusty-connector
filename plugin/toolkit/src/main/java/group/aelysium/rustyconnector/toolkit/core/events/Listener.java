package group.aelysium.rustyconnector.toolkit.core.events;

public interface Listener<Event extends group.aelysium.rustyconnector.toolkit.core.events.Event> {
    void handler(Event event);
}
