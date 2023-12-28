package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player switching from one MCLoader in a family to another MCLoader in that same family.
 */
public class FamilyInternalSwitchEvent extends Cancelable {
    protected final Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family;
    protected final MCLoader previousMCLoader;
    protected final MCLoader newMCLoader;
    protected final Player player;

    public FamilyInternalSwitchEvent(Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family, MCLoader previousMCLoader, MCLoader newMCLoader, Player player) {
        this.family = family;
        this.previousMCLoader = previousMCLoader;
        this.newMCLoader = newMCLoader;
        this.player = player;
    }

    public Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family() {
        return family;
    }
    public MCLoader previousMCLoader() {
        return previousMCLoader;
    }
    public MCLoader newMCLoader() {
        return newMCLoader;
    }
    public Player player() {
        return player;
    }
}