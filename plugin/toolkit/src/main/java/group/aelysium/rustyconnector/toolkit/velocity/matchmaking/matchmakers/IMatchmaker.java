package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.matchmakers;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedGame;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMatchmaker extends Service {
    /**
     * Using the players contained in the matchmaker, attempt to make a game.
     */
    ISession.IWaiting make();

    /**
     * Checks if there is the bare minimum worth of players in the matchmaker necessary to create at least one game.
     * @return `true` if there are at least enough players to make a single game. `false` otherwise.
     */
    boolean minimumPlayersExist();

    /**
     * Completely sort all players in the matchmaker based on their rank in order from least to greatest.
     * The sorting algorithm is a weighted quicksort.
     */
    void completeSort();

    /**
     * Inserts a player into the matchmaker.
     * <p>
     * Specifically, this method will resolve the passed player into a {@link IRankedPlayer}.
     * This method performs a single sort and injects the player into an approximation of the best place for them to reside.
     * Thus reducing how frequently you'll need to perform a full sort on the metchmaker.
     * @param request The request being made.
     * @throws RuntimeException If there was an issue while adding the player to this matchmaker.
     */
    void add(PlayerConnectable.Request request, CompletableFuture<ConnectionResult> result);

    /**
     * Removes the player from the matchmaker.
     * This will remove the player from the matchmaking queue or from a session if they're in one.
     * @param player The player to remove.
     */
    boolean remove(IPlayer player);

    /**
     * Gets The number of players currently waiting in the matchmaker.
     * @return The number of players waiting.
     */
    List<IRankedPlayer> waitingPlayers();

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
     * Fetches a session based on a UUID.
     * @param uuid The uuid to search for.
     * @return A session if it exists. Otherwise, an empty Optional.
     */
    Optional<ISession> fetch(UUID uuid);

    record Settings (
            Ranking ranking,
            Session session,
            Queue queue
    ) {
        public record Ranking(IScoreCard.IRankSchema.Type<?> algorithm, double variance) {}
        public record Session(Building building, Closing closing) {
            public record Building(int min, int max, LiquidTimestamp interval) {}
            public record Closing(int threshold, boolean quittersLose, boolean stayersWin) {}
        }
        public record Queue(Joining joining, Leaving leaving) {
            public record Joining(boolean showInfo, boolean reconnect) {}
            public record Leaving(boolean command, boolean boot) {}
        }
    }
}