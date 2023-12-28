package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player leaving a family.
 * This event will only fire once {@link FamilyPostJoinEvent} has fired.
 * It can be assumed that if this event fires, the player has successfully acquired a new origin.
 * This event will also fire if a player leaves the family by logging out of the network.
 */
public class FamilyLeaveEvent extends Cancelable {
    protected final Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family;
    protected final MCLoader mcLoader;
    protected final Player player;
    protected final boolean disconnected;

    public FamilyLeaveEvent(Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family, MCLoader mcLoader, Player player, boolean disconnected) {
        this.family = family;
        this.mcLoader = mcLoader;
        this.player = player;
        this.disconnected = disconnected;
    }

    public Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family() {
        return family;
    }
    public MCLoader mcLoader() {
        return mcLoader;
    }
    public Player player() {
        return player;
    }
    public boolean disconnected() {
        return disconnected;
    }
}