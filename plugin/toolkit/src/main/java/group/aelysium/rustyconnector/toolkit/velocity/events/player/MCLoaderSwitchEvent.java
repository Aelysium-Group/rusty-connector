package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player switching from one mcloader to another.
 * This event doesn't care about what family the mcloaders are a part of.
 */
public class MCLoaderSwitchEvent extends Cancelable {
    protected final MCLoader oldMCLoader;
    protected final MCLoader newMCLoader;
    protected final Player player;

    public MCLoaderSwitchEvent(MCLoader oldMCLoader, MCLoader newMCLoader, Player player) {
        this.oldMCLoader = oldMCLoader;
        this.newMCLoader = newMCLoader;
        this.player = player;
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