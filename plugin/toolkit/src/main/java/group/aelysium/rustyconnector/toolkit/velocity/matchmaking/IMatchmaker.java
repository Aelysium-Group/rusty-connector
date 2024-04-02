package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMatchmaker<PlayerRank extends IPlayerRank> extends Service {
    /**
     * Gets the game id used by this matchmaker to handle player ranks.
     */
    String gameId();

    /**
     * Inserts a player into the matchmaker.
     * <p>
     * This method will connect the player to an already active session that they are the appropriate rank for (if it exists).
     * If no session exists for them, this method will create a new one for them.
     * @param request The request being made.
     * @throws RuntimeException If there was an issue while adding the player to this matchmaker.
     */
    void queue(PlayerConnectable.Request request, CompletableFuture<ConnectionResult> result);

    /**
     * Removes the player from the matchmaker.
     * This will remove the player from the matchmaking queue or from a session if they're in one.
     * @param player The player to remove.
     */
    boolean remove(IPlayer player);

    /**
     * Checks if a player is currently waiting in the matchmaker.
     * @param player The player to look for.
     * @return `true` if the player is waiting in the matchmaker. `false` otherwise.
     */
    boolean contains(IPlayer player);

    /**
     * Ends a session.
     * This method will close the session, connect all players to the parent family, and unlock the MCLoader.
     * @param session The session to end.
     */
    void remove(ISession session);

    /**
     * Fetches a session based on a player's UUID.
     * @param uuid The uuid to search for.
     * @return A session if it exists. Otherwise, an empty Optional.
     */
    Optional<ISession> fetchPlayerSession(UUID uuid);

    /**
     * Fetches a session based on a UUID.
     * @param uuid The uuid to search for.
     * @return A session if it exists. Otherwise, an empty Optional.
     */
    Optional<ISession> fetch(UUID uuid);

    /**
     * Returns the total number of players in the Matchmaker.
     */
    int playerCount();

    /**
     * Returns the number of players waiting in queue.
     */
    int queuedPlayerCount();

    /**
     * Returns the number of players in an active session.
     */
    int activePlayerCount();

    /**
     * Returns the total number of sessions in the Matchmaker.
     */
    int sessionCount();

    /**
     * Returns the number of sessions waiting in queue.
     */
    int queuedSessionCount();

    /**
     * Returns the number of sessions that are active.
     */
    int activeSessionCount();

    record Settings (
            Ranking ranking,
            Session session,
            Queue queue
    ) {
        public record Ranking(Class<? extends IPlayerRank> schema, double variance) {}
        public record Session(
                boolean freezeActiveSessions,
                int min,
                int max,
                LiquidTimestamp interval,
                int closingThreshold,
                boolean quittersLose,
                boolean stayersWin
        ) {}
        public record Queue(Joining joining, Leaving leaving) {
            public record Joining(boolean showInfo, boolean reconnect) {}
            public record Leaving(boolean command, boolean boot) {}
        }
    }
}