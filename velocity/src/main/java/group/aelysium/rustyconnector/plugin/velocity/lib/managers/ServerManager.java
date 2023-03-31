package group.aelysium.rustyconnector.plugin.velocity.lib.managers;

import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager implements NodeManager<PlayerServer> {
    private final Map<String, PlayerServer> registeredServers = new HashMap<>();


    /**
     * Get a server via its name.
     * @param name The name of the server to get.
     * @return A server.
     */
    @Override
    public PlayerServer find(String name) {
        return this.registeredServers.get(name);
    }

    /**
     * Add a server to this manager.
     * @param server The server to add to this manager.
     */
    @Override
    public void add(PlayerServer server) {
        this.registeredServers.put(server.getRegisteredServer().getServerInfo().getName(), server);
    }

    /**
     * Remove a server from this manager.
     * @param server The server to remove from this manager.
     */
    @Override
    public void remove(PlayerServer server) {
        this.registeredServers.remove(server.getRegisteredServer().getServerInfo().getName());
    }

    @Override
    public List<PlayerServer> dump() {
        return this.registeredServers.values().stream().toList();
    }

    @Override
    public void clear() {
        this.registeredServers.clear();
    }

}
