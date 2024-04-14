package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public class OnMCLoaderLeave implements Listener<MCLoaderLeaveEvent> {
    public void handler(MCLoaderLeaveEvent event) {
        IMCLoader mcLoader = event.mcLoader();

        mcLoader.leave(event.player());
    }
}