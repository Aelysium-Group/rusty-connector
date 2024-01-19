package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents a player leaving an MCLoader.
 * This event will only fire once {@link FamilyLeaveEvent} has fired.
 * It can be assumed that if this event fires, the player has successfully acquired a new origin.
 * This event will also fire if a player leaves the family by logging out of the network.
 */
public class MCLoaderLeaveEvent implements Event {
    protected final IMCLoader mcLoader;
    protected final IPlayer player;
    protected final boolean disconnected;

    public MCLoaderLeaveEvent(IMCLoader mcLoader, IPlayer player, boolean disconnected) {
        this.mcLoader = mcLoader;
        this.player = player;
        this.disconnected = disconnected;
    }

    public IMCLoader mcLoader() {
        return mcLoader;
    }
    public IPlayer player() {
        return player;
    }
    public boolean disconnected() {
        return disconnected;
    }
}