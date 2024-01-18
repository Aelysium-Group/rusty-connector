package group.aelysium.rustyconnector.core.mcloader.event_handlers;

import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.magic_link.ConnectedEvent;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

public class OnConnection extends Listener<ConnectedEvent> {
    @Override
    @Handler(delivery = Invoke.Asynchronously) // Changes priority to {@link Priority.NATIVE}
    public void handler(ConnectedEvent event) {
        System.out.println("registered!");
    }
}