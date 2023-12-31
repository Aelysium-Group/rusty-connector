package group.aelysium.rustyconnector.core.mcloader.events;

import group.aelysium.rustyconnector.toolkit.core.events.Listener;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game.RankedGameEndEvent;
import net.engio.mbassy.listener.Handler;

public class OnRankedGameEnd extends Listener<RankedGameEndEvent> {
    @Override
    @Handler() // Changes priority to {@link Priority.NATIVE}
    public void handler(RankedGameEndEvent event) {
    }
}