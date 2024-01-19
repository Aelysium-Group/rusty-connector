package group.aelysium.rustyconnector.toolkit.core.events;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

public class DefaultListener extends Listener<Cancelable> {
    @Override
    @Handler(delivery = Invoke.Asynchronously) // Changes priority to {@link Priority.NATIVE}
    public void handler(Cancelable event) {
    }
}