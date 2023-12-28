package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player successfully connecting to a family.
 */
public class FamilyPostJoinEvent extends Cancelable {
    protected final Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family;
    protected final MCLoader mcLoader;
    protected final Player player;

    public FamilyPostJoinEvent(Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family, MCLoader mcLoader, Player player) {
        this.family = family;
        this.mcLoader = mcLoader;
        this.player = player;
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
}