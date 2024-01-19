package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay;

import group.aelysium.rustyconnector.toolkit.core.JSONParseable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IGamemodeRankManager;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

import java.rmi.AlreadyBoundException;
import java.util.List;
import java.util.UUID;

public interface ISession extends JSONParseable {
    UUID uuid();

    Settings settings();

    /**
     * Ends this session.
     */
    void end(List<UUID> winners, List<UUID> losers);

    /**
     * Implodes the session.
     * This method is similar to {@link #end(List, List)} except that it will inform players that their session had to be ended,
     * and not players will be rewarded points.
     */
    void implode();

    IRankedMCLoader mcLoader();

    /**
     * Gets the players that are currently in this session.
     */
    List<IPlayer> players();


    interface IWaiting {
        UUID uuid();

        /**
         * Starts the session on the specified MCLoader.
         * By the time {@link ISession} is returned, it should be assumed that all players have connected.
         * @param mcLoader The MCLoader to run the session on.
         * @param settings The settings that governs this session.
         * @return A running {@link ISession}.
         * @throws AlreadyBoundException If a session is already running on this MCLoader.
         */
        ISession start(IRankedMCLoader mcLoader, Settings settings) throws AlreadyBoundException;

        /**
         * Checks if the waiting session contains the player.
         */
        boolean contains(IPlayer player);
    }

    record Settings(int min, int max, IGamemodeRankManager game) {}
}
