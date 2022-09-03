package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.core.generic.lib.generic.Lang;
import group.aelysium.rustyconnector.core.generic.lib.generic.load_balancing.Algorithm;
import group.aelysium.rustyconnector.core.generic.lib.generic.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.core.generic.lib.generic.server.Family;
import group.aelysium.rustyconnector.core.generic.lib.generic.server.Server;
import group.aelysium.rustyconnector.core.generic.lib.generic.whitelist.Whitelist;
import group.aelysium.rustyconnector.core.generic.lib.generic.whitelist.WhitelistPlayer;

import java.util.*;

public class ServerFamily implements Family {
    private final Map<ServerInfo,PaperServer> registeredServers = new HashMap<>();
    private String name;
    protected int playerCount = 0;
    protected AlgorithmType algorithm;
    private Whitelist whitelist;
    public ServerFamily(String name, AlgorithmType algorithm, Whitelist whitelist) {
        this.name = name;
        this.algorithm = algorithm;
        this.whitelist = whitelist;
    }

    public String algorithm() { return this.algorithm.toString(); }
    public int serverCount() { return this.registeredServers.size(); }
    public int playerCount() { return this.registeredServers.size(); }

    /**
     * Connect a player to this family
     * @param player The player to connect
     */
    public void connect(Player player) {
        if(this.whitelist != null) {
            String ip = player.getRemoteAddress().getHostString();

            WhitelistPlayer whitelistPlayer = new WhitelistPlayer(player.getUsername(), player.getUniqueId(), ip);

            if (!whitelist.validate(whitelistPlayer))
                player.disconnect(Lang.getDynamic("When Player Isn't Whitelisted"));
        }

        PaperServer server = (PaperServer) Algorithm.getAlgorithm(this.algorithm).processConnection(this);

        if(server.isFull())
            if(!player.hasPermission("rustyconnector.bypasssoftcap"))
                return;
        if(server.isMaxed())
            return;

        player.createConnectionRequest(server.getRawServer());
    }

    @Override
    public Map<Object, Server> getRegisteredServers() {
        return new HashMap<>(); // TODO: sort this out
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void registerServer(Server server) {
        PaperServer paperServer = (PaperServer) server;
        this.registeredServers.put(paperServer.getRawServer().getServerInfo(), paperServer);
        VelocityRustyConnector.getInstance().getVelocityServer().unregisterServer(paperServer.getRawServer().getServerInfo());
    }

    /**
     * Unregisters a server from this family.
     * @param serverInfo The info matching the server to unregister
     */
    public void unregisterServer(ServerInfo serverInfo) {
        this.registeredServers.remove(serverInfo);
        VelocityRustyConnector.getInstance().getVelocityServer().unregisterServer(serverInfo);
    }

    /**
     * Gets a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     * @throws NullPointerException If the server can't be found.
     */
    public PaperServer getServer(ServerInfo serverInfo) throws NullPointerException {
        return this.registeredServers.get(serverInfo);
    }

    public void printInfo() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        Lang.print(plugin.logger(), Lang.get("info"));
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("Family Info");
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("   ---| Name: "+this.getName());
        plugin.logger().log("   ---| Online Players: "+this.playerCount());
        plugin.logger().log("   ---| Registered Servers: "+this.serverCount());
        plugin.logger().log("   ---| Load Balancing Algorithm: "+this.algorithm());
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
    }

    public void printServers() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        Lang.print(plugin.logger(), Lang.get("info"));
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("All servers registered to the family: "+this.name);
        plugin.logger().log("Registered Servers: "+this.serverCount());
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
        plugin.logger().log(Lang.spacing());
        this.registeredServers.forEach((info, data) -> {
            plugin.logger().log("   ---| "+info.getName());
        });
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
    }
}
