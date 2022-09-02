package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

import java.net.InetSocketAddress;

public class ServerManager {
    private VelocityRustyConnector plugin;

    public ServerManager(VelocityRustyConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new server which can be registered to the proxy.
     * @param name The name to be assigned to the server. Must be unique.
     * @param hostname The hostname of the server.
     * @param port TThe port number of the server.
     */
    public ServerInfo createServer(String name, String hostname, int port) {
        InetSocketAddress address = new InetSocketAddress(hostname, port);
        return new ServerInfo(
            name,
            address
        );
    }
    public void registerServer(ServerInfo server) {
        this.plugin.getVelocityServer().registerServer(server);
    }
    public void unregisterServer(ServerInfo server) {
        this.plugin.getVelocityServer().unregisterServer(server);
    }
}
