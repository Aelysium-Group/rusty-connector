package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents a player joining an MCLoader.
 * This event will only fire once {@link FamilyPostJoinEvent} has fired.
 */
public class MCLoaderJoinEvent implements Event {
    protected final IMCLoader mcLoader;
    protected final IPlayer player;

    public MCLoaderJoinEvent(IMCLoader mcLoader, IPlayer player) {
        this.mcLoader = mcLoader;
        this.player = player;
    }

    public IMCLoader mcLoader() {
        return mcLoader;
    }
    public IPlayer player() {
        return player;
    }
}