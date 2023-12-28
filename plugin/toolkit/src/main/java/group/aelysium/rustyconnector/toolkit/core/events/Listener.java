package group.aelysium.rustyconnector.toolkit.core.events;

import net.engio.mbassy.listener.Handler;

@net.engio.mbassy.listener.Listener
public abstract class Listener<Event extends Cancelable> {
    @Handler(priority = Priority.AFTER_NATIVE)
    @CancelableHandler
    public abstract void handler(Event event);
}
