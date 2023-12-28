package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player attempting to connect to a family.
 * <p>
 * Please note, if the family that fires this is a {@link RankedFamily}, this will fire
 * when the player is added to the matchmaker.
 * In such a case, {@link FamilyPostJoinEvent} won't be called until the player actually joins a game.
 */
public class FamilyPreJoinEvent extends Cancelable {
    protected Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family;
    protected Player player;

    public FamilyPreJoinEvent(Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family, Player player) {
        this.family = family;
        this.player = player;
    }

    public Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family() {
        return family;
    }
    public Player player() {
        return player;
    }
}