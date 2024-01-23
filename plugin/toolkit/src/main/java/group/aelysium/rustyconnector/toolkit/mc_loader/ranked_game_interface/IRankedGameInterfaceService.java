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
    Optional<Map<UUID, String>> players();

    /**
     * Ends the session with the defined players marked as winners.
     * If there is no active session, nothing will happen.
     * If you don't want a specific player to receive either a win or a lose, simply don't pass their uuid to the method.
     * @param winners A list of the players that are winners. These players will have a win added to their scorecard.
     * @param losers A list of players that are losers. These players will have a loss added to their scorecard.
     */
    void end(List<UUID> winners, List<UUID> losers);
}
