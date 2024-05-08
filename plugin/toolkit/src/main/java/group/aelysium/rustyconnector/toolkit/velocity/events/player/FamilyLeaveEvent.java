package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents a player leaving a family.
 * This event will only fire once {@link FamilyPostJoinEvent} has fired.
 * It can be assumed that if this event fires, the player has successfully acquired a new origin.
 * This event will also fire if a player leaves the family by logging out of the network.
 */
public class FamilyLeaveEvent implements Event {
    protected final IFamily family;
    protected final IMCLoader mcLoader;
    protected final IPlayer player;
    protected final boolean disconnected;

    public FamilyLeaveEvent(IFamily family, IMCLoader mcLoader, IPlayer player, boolean disconnected) {
        this.family = family;
        this.mcLoader = mcLoader;
        this.player = player;
        this.disconnected = disconnected;
    }

    public IFamily family() {
        return family;
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