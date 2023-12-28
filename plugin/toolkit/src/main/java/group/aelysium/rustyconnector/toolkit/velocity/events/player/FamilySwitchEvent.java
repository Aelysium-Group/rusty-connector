package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player switching from one family to another family.
 * Specifically, this event will fire after {@link FamilyLeaveEvent} is fired on the previous family, and after {@link FamilyPostJoinEvent} fires on the new family.
 */
public class FamilySwitchEvent extends Cancelable {
    protected final Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> oldFamily;
    protected final Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> newFamily;
    protected final MCLoader oldMCLoader;
    protected final MCLoader newMCLoader;
    protected final Player player;

    public FamilySwitchEvent(Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> oldFamily, Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> newFamily, MCLoader oldMCLoader, MCLoader newMCLoader, Player player) {
        this.oldFamily = oldFamily;
        this.newFamily = newFamily;
        this.oldMCLoader = oldMCLoader;
        this.newMCLoader = newMCLoader;
        this.player = player;
    }

    public Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> oldFamily() {
        return oldFamily;
    }
    public Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> newFamily() {
        return newFamily;
    }
    public MCLoader oldMCLoader() {
        return oldMCLoader;
    }
    public MCLoader newMCLoader() {
        return newMCLoader;
    }
    public Player player() {
        return player;
    }
}