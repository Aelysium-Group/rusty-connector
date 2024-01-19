package group.aelysium.rustyconnector.toolkit.core.events;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

@net.engio.mbassy.listener.Listener
public abstract class Listener<Event extends Cancelable> {
    @Handler(priority = Priority.AFTER_NATIVE, delivery = Invoke.Asynchronously)
    public abstract void handler(Event event);
}
