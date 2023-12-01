package group.aelysium.rustyconnector.toolkit.velocity.family.ranked_family;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IRankedFamily<TMCLoader extends IMCLoader, TPlayer extends IPlayer> extends IFamily<TMCLoader, TPlayer> {
    /**
     * Queues a player into this family's matchmaking.
     * The player will be connected once a match has been made.
     * The player's queue to this matchmaker will not timeout.
     * You must manually call {@link #dequeue(TPlayer)} to remove a player from this queue.
     * @param player The player to connect.
     * @return null. Always.
     */
    @Override
    TMCLoader connect(TPlayer player);

    /**
     * Dequeues a player from this family's matchmaking.
     * If the player is already connected to this family, nothing will happen.
     * @param player The player to dequeue.
     */
    void dequeue(TPlayer player);


}
