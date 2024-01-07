package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents a player joining the network.
 * This event fires after {@link FamilyLeaveEvent}.
 */
public class NetworkLeaveEvent extends Cancelable {
    protected final IFamily lastFamily;
    protected final IMCLoader lastMCLoader;
    protected final IPlayer player;

    public NetworkLeaveEvent(IFamily lastFamily, IMCLoader lastMCLoader, IPlayer player) {
        this.lastFamily = lastFamily;
        this.lastMCLoader = lastMCLoader;
        this.player = player;
    }

    public IFamily lastFamily() {
        return lastFamily;
    }
    public IMCLoader lastMCLoader() {
        return lastMCLoader;
    }
    public IPlayer player() {
        return player;
    }
}