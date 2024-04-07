package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import group.aelysium.rustyconnector.toolkit.core.JSONParseable;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

import java.rmi.AlreadyBoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    void implode(String reason);

    Optional<IRankedMCLoader> mcLoader();

    RankRange range();

    int size();

    /**
     * Gets whether this session will allow new players to join it.
     */
    boolean frozen();

    /**
     * Gets whether this session is active.
     * A session is considered active if it is connected ot an MCLoader.
     * @return `true` if the session is active. `false` otherwise.
     */
    boolean active();

    /**
     * Gets whether this session is full.
     */
    boolean full();

    /**
     * Checks if the running session contains the player.
     */
    boolean contains(IMatchPlayer<IPlayerRank> player);

    /**
     * Gets the players that are currently in this session.
     */
    Map<UUID, IMatchPlayer<IPlayerRank>> players();

    /**
     * Removes the player from the session.
     * The session will implode if the player leaving causes it to have not enough players to continue.
     * @param player The player to leave.
     */
    boolean leave(IMatchPlayer<IPlayerRank> player);


    /**
     * Adds the player to the session.
     * If the session is currently active the player will also attempt to connect to it.
     * @param player The player to join.
     */
    PlayerConnectable.Request join(IMatchPlayer<IPlayerRank> player);

    /**
     * Starts the session on the specified MCLoader.
     * Once the session starts, all players will be connected to the MCLoader.
     * Additionally, any players that join the session after it's started will also connect to the MCLoader instantly.
     * @param mcLoader The MCLoader to run the session on.
     * @throws AlreadyBoundException If a session is already running on this MCLoader.
     */
    void start(IRankedMCLoader mcLoader) throws AlreadyBoundException;

    class RankRange {
        private final double pivot;
        private final double min;
        private final double max;

        public RankRange(double pivot, double variance) {
            this.pivot = pivot;

            double con = pivot * variance;
            double max = pivot + con;
            double min = pivot - con;
            this.min = min;
            this.max = max;
        }

        public double pivot() {
            return this.pivot;
        }

        public double min() {
            return this.min;
        }

        public double max() {
            return this.max;
        }

        public boolean validate(double rank) {
            return rank > min && rank < max;
        }
    }

    record Settings(boolean shouldFreeze, int min, int max, double variance, String gameId) {}
}
