package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.lib.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

public abstract class BaseServerFamily implements IBaseFamily<PlayerServer> {
    protected final String name;

    protected BaseServerFamily(String name) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    /**
     * Get a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     * @return A found server or `null` if there's no match.
     */
    abstract public PlayerServer findServer(@NotNull ServerInfo serverInfo);

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    abstract public void addServer(PlayerServer server);

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    abstract public void removeServer(PlayerServer server);

    /**
     * Get all players in the family up to approximately `max`.
     * @param max The approximate max number of players to return.
     * @return A list of players.
     */
    abstract public List<Player> players(int max);

    abstract public List<PlayerServer> registeredServers();

    public boolean containsServer(ServerInfo serverInfo) {
        return !(this.findServer(serverInfo) == null);
    }

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    abstract public long playerCount();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseServerFamily that = (BaseServerFamily) o;
        return Objects.equals(name, that.name);
    }
}
