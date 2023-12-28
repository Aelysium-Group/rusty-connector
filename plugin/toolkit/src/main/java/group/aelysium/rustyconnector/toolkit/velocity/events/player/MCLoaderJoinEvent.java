package group.aelysium.rustyconnector.toolkit.velocity.events.player;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a player joining an MCLoader.
 * This event will only fire once {@link FamilyPostJoinEvent} has fired.
 */
public class MCLoaderJoinEvent extends Cancelable {
    protected final MCLoader mcLoader;
    protected final Player player;

    public MCLoaderJoinEvent(MCLoader mcLoader, Player player) {
        this.mcLoader = mcLoader;
        this.player = player;
    }

    public MCLoader mcLoader() {
        return mcLoader;
    }
    public Player player() {
        return player;
    }
}