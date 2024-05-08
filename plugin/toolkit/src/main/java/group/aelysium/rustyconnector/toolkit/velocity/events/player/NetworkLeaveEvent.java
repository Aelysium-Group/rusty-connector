package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

/**
 * Represents a player joining the network.
 * This event fires after {@link FamilyLeaveEvent}.
 */
public class NetworkLeaveEvent implements Event {
    protected final IPlayer player;

    public NetworkLeaveEvent(IPlayer player) {
        this.player = player;
    }

    public IPlayer player() {
        return player;
    }
}