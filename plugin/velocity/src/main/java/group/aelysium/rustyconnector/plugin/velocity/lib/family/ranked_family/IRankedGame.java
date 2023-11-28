package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;

import java.util.UUID;

public interface IRankedGame {
    UUID uuid();

    /**
     * Connects the game to the specified server and marks that server as this game's server.
     * @param server The server.
     */
    void connectServer(MCLoader server);

    /**
     * Returns whatever the server of this game is.
     * @return {@link MCLoader} or `null` if there isn't one.
     */
    MCLoader server();

    /**
     * Returns if the game has ended already.
     * @return `true` if the game has ended. `false` otherwise.
     */
    boolean ended();
}
