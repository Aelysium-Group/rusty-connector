package group.aelysium.rustyconnector.toolkit.velocity.family.ranked_family;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

public interface IRankedFamily extends IFamily, Service {
    /**
     * Queues a player into this family's matchmaking.
     * The player will be connected once a match has been made.
     * The player's queue to this matchmaker will not timeout.
     * You must manually call {@link #leave(IPlayer)} to remove a player from this queue.
     * <p>
     * The returned {@link Request} will never resolve.
     * @param player The player to connect.
     * @return null. Always.
     */
    Request connect(IPlayer player);

    IMatchmaker matchmaker();
}
