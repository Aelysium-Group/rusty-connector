package group.aelysium.rustyconnector.plugin.velocity.lib.managers;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.core.lib.util.logger.GateKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager implements NodeManager<PaperServer> {
    private final Map<String, PaperServer> registeredServers = new HashMap<>();


    /**
     * Get a server via its name.
     * @param name The name of the server to get.
     * @return A server.
     */
    @Override
    public PaperServer find(String name) {
        return this.registeredServers.get(name);
    }

    /**
     * Add a server to this manager.
     * @param server The server to add to this manager.
     */
    @Override
    public void add(PaperServer server) {
        this.registeredServers.put(server.getRegisteredServer().getServerInfo().getName(), server);
    }

    /**
     * Remove a server from this manager.
     * @param server The server to remove from this manager.
     */
    @Override
    public void remove(PaperServer server) {
        this.registeredServers.remove(server.getRegisteredServer().getServerInfo().getName());
    }

    @Override
    public List<PaperServer> dump() {
        return this.registeredServers.values().stream().toList();
    }

}
