package group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IRankedGameInterfaceService extends Service {
    Optional<UUID> uuid();

    /**
     * Gets the players that are currently in this session.
     */
    Optional<Map<UUID, MCLoaderMatchPlayer>> players();

    /**
     * Ends the session with the defined players marked as winners.
     * If there is no active session, nothing will happen.
     * If you don't want a specific player to receive either a win or a lose, simply don't pass their uuid to the method.
     * @param winners A list of the players that are winners. These players will have a win added to their scorecard.
     * @param losers A list of players that are losers. These players will have a loss added to their scorecard.
     */
    void end(List<UUID> winners, List<UUID> losers);

    /**
     * Ends the session with the defined players marked as winners.
     * If there is no active session, nothing will happen.
     * If you don't want a specific player to receive either a win or a lose, simply don't pass their uuid to the method.
     * @param winners A list of the players that are winners. These players will have a win added to their scorecard.
     * @param losers A list of players that are losers. These players will have a loss added to their scorecard.
     * @param unlock Whether the MCLoader should unlock right away. If `false` the MCLoader will have to manually unlock itself.
     */
    void end(List<UUID> winners, List<UUID> losers, boolean unlock);

    /**
     * Ends the session in a tie.
     * If there is no active session, nothing will happen.
     * All players in the session will receive a tie. If the game's ranking algorithm supports ties, this may impact their rank.
     * If you want to end a game with a guarantee to not impact a player's rank, you can use {@link IRankedGameInterfaceService#end(List, List)} with an empty list for each parameter.
     */
    void endInTie();

    /**
     * Ends the session in a tie.
     * If there is no active session, nothing will happen.
     * All players in the session will receive a tie. If the game's ranking algorithm supports ties, this may impact their rank.
     * If you want to end a game with a guarantee to not impact a player's rank, you can use {@link IRankedGameInterfaceService#end(List, List)} with an empty list for each parameter.
     * @param unlock Whether the MCLoader should unlock right away. If `false` the MCLoader will have to manually unlock itself.
     */
    void endInTie(boolean unlock);

    /**
     * Manually force the session to end.
     * Implosion may or may not affect player ranks based on what's defined in the matchmaker.yml
     * @param reason The reason for the implosion. This reason will be sent to the players.
     */
    void implode(String reason);

    /**
     * Manually force the session to end.
     * Implosion may or may not affect player ranks based on what's defined in the matchmaker.yml
     * @param reason The reason for the implosion. This reason will be sent to the players.
     * @param unlock Whether the MCLoader should unlock right away. If `false` the MCLoader will have to manually unlock itself.
     */
    void implode(String reason, boolean unlock);
}
