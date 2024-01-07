package group.aelysium.rustyconnector.core.lib.events;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import net.engio.mbassy.bus.MBassador;

import java.util.concurrent.TimeUnit;

public class EventManager implements group.aelysium.rustyconnector.toolkit.core.events.EventManager {
    protected MBassador<Event> manager = new MBassador<>();

    public void on(Object listener) {
        this.manager.subscribe(listener);
    }
    public void off(Object listener) {
        this.manager.unsubscribe(listener);
    }
    public void fire(Event event) {
        this.manager.publish(event);
    }
    public void fireAndForget(Event event) {
        this.manager.publishAsync(event);
    }
    public void fireWithTimeout(Event event) {
        this.manager.publishAsync(event, 10, TimeUnit.SECONDS);
    }

    @Override
    public void kill() {
        this.manager.shutdown();
    }
}