package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import group.aelysium.rustyconnector.toolkit.core.JSONParseable;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
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
     * Has the session ended
     */
    boolean ended();

    /**
     * Gets the matchmaker that this session belongs to.
     */
    IMatchmaker matchmaker();

    /**
     * Ends this session with a set of winners and losers.
     * @param winners The uuids of the winning players.
     * @param losers The uuids of the losing players.
     */
    void end(List<UUID> winners, List<UUID> losers);

    /**
     * Ends this session with a set of winners and losers.
     * @param winners The uuids of the winning players.
     * @param losers The uuids of the losing players.
     * @param unlock Whether the MCLoader should unlock right away. If `false` the MCLoader will have to manually unlock itself.
     */
    void end(List<UUID> winners, List<UUID> losers, boolean unlock);

    /**
     * Ends this session in a tie.
     * All players will receive a "tie" if the ranking algorithm supports ties
     * this might affect their rank.
     */
    void endTied();

    /**
     * Ends this session in a tie.
     * All players will receive a "tie" if the ranking algorithm supports ties
     * this might affect their rank.
     * @param unlock Whether the MCLoader should unlock right away. If `false` the MCLoader will have to manually unlock itself.
     */
    void endTied(boolean unlock);

    /**
     * Implodes the session.
     * This method is similar to {@link ISession#end(List, List)} except that it will inform players that their session had to be ended,
     * and not players will be rewarded points.
     * @param reason The reason for the implosion. This reason will also be shown to the players.
     */
    void implode(String reason);

    /**
     * Implodes the session.
     * This method is similar to {@link ISession#end(List, List)} except that it will inform players that their session had to be ended,
     * and not players will be rewarded points.
     * @param reason The reason for the implosion. This reason will also be shown to the players.
     * @param unlock Whether the MCLoader should unlock right away. If `false` the MCLoader will have to manually unlock itself.
     */
    void implode(String reason, boolean unlock);

    Optional<IRankedMCLoader> mcLoader();

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
    boolean contains(IMatchPlayer player);

    /**
     * Gets the players that are currently in this session.
     */
    Map<UUID, IMatchPlayer> players();

    /**
     * Adds the player to the session.
     * If the session is currently active the player will also attempt to connect to it.
     * @param player The player to join.
     */
    PlayerConnectable.Request join(IMatchPlayer player);

    /**
     * Starts the session on the specified MCLoader.
     * Once the session starts, all players will be connected to the MCLoader.
     * Additionally, any players that join the session after it's started will also connect to the MCLoader instantly.
     * @param mcLoader The MCLoader to run the session on.
     * @throws AlreadyBoundException If a session is already running on this MCLoader.
     */
    void start(IRankedMCLoader mcLoader) throws AlreadyBoundException;

    /**
     * Empties all players out of this session.
     */
    void empty();

    record Settings(boolean shouldFreeze, int min, int max, double variance, String gameId, boolean quittersLose, boolean stayersWin) {}
}
