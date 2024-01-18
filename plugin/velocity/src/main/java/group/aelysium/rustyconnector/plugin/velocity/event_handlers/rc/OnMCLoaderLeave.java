package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.MCLoaderLeaveEvent;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

public class OnMCLoaderLeave extends Listener<MCLoaderLeaveEvent> {
    @Override
    @Handler(delivery = Invoke.Asynchronously) // Changes priority to {@link Priority.NATIVE}
    public void handler(MCLoaderLeaveEvent event) {
        IMCLoader mcLoader = event.mcLoader();

        mcLoader.leave(event.player());
        try {
            RankedFamily family = (RankedFamily) mcLoader.family();

            if(!family.matchmaker().contains(event.player())) return;

            family.matchmaker().remove(event.player());
        } catch (Exception ignore) {}
    }
}