package group.aelysium.rustyconnector.toolkit.common.events;

public interface Listener<Event extends group.aelysium.rustyconnector.toolkit.common.events.Event> {
    void handler(Event event);
}
