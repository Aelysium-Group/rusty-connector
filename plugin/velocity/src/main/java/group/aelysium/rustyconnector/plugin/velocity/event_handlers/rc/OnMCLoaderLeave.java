package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderLeaveEvent;
import net.engio.mbassy.listener.Handler;

public class OnMCLoaderLeave extends Listener<MCLoaderLeaveEvent> {
    @Override
    @Handler() // Changes priority to {@link Priority.NATIVE}
    public void handler(MCLoaderLeaveEvent event) {

        event.mcLoader().leave(event.player());
    }
}