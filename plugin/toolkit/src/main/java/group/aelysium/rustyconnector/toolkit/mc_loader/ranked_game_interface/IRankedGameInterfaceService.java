package group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRankedGameInterfaceService extends Service {
    Optional<UUID> uuid();

    /**
     * Gets the players that are currently in this session.
     */
    Optional<List<UUID>> players();

    /**
     * Ends the session with the defined players marked as winners.
     * If there is no active session, nothing will happen.
     * Anybody not listed will be marked as losers.
     * This method will also cause players to be sent back to the parent family.
     * @param winners A list of the players that are winners. All players in this session that aren't in this list will be marked as losers.
     */
    void end(List<UUID> winners);
}
