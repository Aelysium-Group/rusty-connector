package group.aelysium.rustyconnector.toolkit.velocity.family.ranked_family;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.connection.ConnectionRequest;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IRankedFamily extends IFamily, Service {
    /**
     * Queues a player into this family's matchmaking.
     * The player will be connected once a match has been made.
     * The player's queue to this matchmaker will not timeout.
     * You must manually call {@link #dequeue(IPlayer)} to remove a player from this queue.
     * <p>
     * The returned {@link ConnectionRequest} will never resolve.
     * @param player The player to connect.
     * @return null. Always.
     */
    ConnectionRequest connect(IPlayer player);

    /**
     * Dequeues a player from this family's matchmaking.
     * If the player is already connected to this family, nothing will happen.
     * @param player The player to dequeue.
     */
    void dequeue(IPlayer player);
}
