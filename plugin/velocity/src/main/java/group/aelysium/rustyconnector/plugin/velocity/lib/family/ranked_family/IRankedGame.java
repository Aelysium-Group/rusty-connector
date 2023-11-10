package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.util.UUID;

public interface IRankedGame {
    UUID uuid();

    /**
     * Connects the game to the specified server and marks that server as this game's server.
     * @param server The server.
     */
    void connectServer(PlayerServer server);

    /**
     * Returns whatever the server of this game is.
     * @return {@link PlayerServer} or `null` if there isn't one.
     */
    PlayerServer server();

    /**
     * Returns if the game has ended already.
     * @return `true` if the game has ended. `false` otherwise.
     */
    boolean ended();
}
