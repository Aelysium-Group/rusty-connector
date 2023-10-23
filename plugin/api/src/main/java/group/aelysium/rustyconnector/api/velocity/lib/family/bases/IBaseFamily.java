package group.aelysium.rustyconnector.api.velocity.lib.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.lib.server.IPlayerServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IBaseFamily<S extends IPlayerServer> {
    String name();

    /**
     * Get a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     * @return A found server or `null` if there's no match.
     */
    S findServer(@NotNull ServerInfo serverInfo);

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    void addServer(S server);

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    void removeServer(S server);

    /**
     * Get all players in the family up to approximately `max`.
     * @param max The approximate max number of players to return.
     * @return A list of players.
     */
    List<Player> players(int max);

    List<S> registeredServers();

    boolean containsServer(ServerInfo serverInfo);

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    long playerCount();
}
