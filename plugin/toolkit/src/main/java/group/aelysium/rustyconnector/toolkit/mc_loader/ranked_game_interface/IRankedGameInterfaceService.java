package group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.UUID;

public interface IRankedGameInterfaceService extends Service {
    /**
     * Stores the uuid of presumably a game that's being tracked by the Proxy.
     * @param uuid The UUID.
     */
    void associateGame(UUID uuid);

    /**
     * Sends a request to the Proxy to end a game theoretically being tracked by the Proxy.
     * The request contains a UUID that's been stored locally via {@link #associateGame(UUID)}.
     */
    void endGame();
}
