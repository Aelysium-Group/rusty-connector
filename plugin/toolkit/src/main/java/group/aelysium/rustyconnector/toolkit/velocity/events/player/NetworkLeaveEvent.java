package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player joining the network.
 * This event fires after {@link FamilyLeaveEvent}.
 */
public class NetworkLeaveEvent extends Cancelable {
    protected final Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> lastFamily;
    protected final MCLoader lastMCLoader;
    protected final Player player;

    public NetworkLeaveEvent(Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> lastFamily, MCLoader lastMCLoader, Player player) {
        this.lastFamily = lastFamily;
        this.lastMCLoader = lastMCLoader;
        this.player = player;
    }

    public Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> lastFamily() {
        return lastFamily;
    }
    public MCLoader lastMCLoader() {
        return lastMCLoader;
    }
    public Player player() {
        return player;
    }
}