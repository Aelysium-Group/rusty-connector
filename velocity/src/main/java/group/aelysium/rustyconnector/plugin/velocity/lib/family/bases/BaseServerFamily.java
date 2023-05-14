package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class BaseServerFamily<S extends PlayerServer> {
    protected final String name;

    protected BaseServerFamily(String name) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Get a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     * @return A found server or `null` if there's no match.
     */
    abstract public S getServer(@NotNull ServerInfo serverInfo);

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    abstract public void addServer(S server);

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    abstract public void removeServer(S server);

    /**
     * Unregisters all servers from this family.
     */
    abstract public void unregisterServers() throws Exception;

    /**
     * Get all players in the family up to approximately `max`.
     * @param max The approximate max number of players to return.
     * @return A list of players.
     */
    abstract public List<Player> getAllPlayers(int max);

    abstract public List<S> getRegisteredServers();

    public boolean containsServer(ServerInfo serverInfo) {
        return !(this.getServer(serverInfo) == null);
    }

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    abstract public long getPlayerCount();
}
