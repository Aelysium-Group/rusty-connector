package group.aelysium.rustyconnector.core.lib.events;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.core.events.EventErrorHandler;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;

import java.util.concurrent.TimeUnit;

public class EventManager implements group.aelysium.rustyconnector.toolkit.core.events.EventManager {
    protected MBassador<Cancelable> manager = new MBassador<>(
            new BusConfiguration()
            .addFeature(Feature.SyncPubSub.Default())
            .addFeature(Feature.AsynchronousHandlerInvocation.Default())
            .addFeature(Feature.AsynchronousMessageDispatch.Default())
            .addPublicationErrorHandler(new EventErrorHandler())
    );

    public void on(Object listener) {
        this.manager.subscribe(listener);
    }
    public void off(Object listener) {
        this.manager.unsubscribe(listener);
    }
    public void fire(Cancelable event) {
        this.manager.post(event).now();
    }
    public void fireAndForget(Cancelable event) {
        this.manager.post(event).asynchronously();
    }
    public void fireWithTimeout(Cancelable event) {
        this.manager.post(event).asynchronously(10, TimeUnit.SECONDS);
    }

    @Override
    public void kill() {
        this.manager.shutdown();
    }
}