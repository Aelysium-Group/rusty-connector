package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.typesafe.config.ConfigException;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import rustyconnector.generic.lib.generic.Lang;
import rustyconnector.generic.lib.generic.load_balancing.Algorithm;
import rustyconnector.generic.lib.generic.load_balancing.AlgorithmType;
import rustyconnector.generic.lib.generic.server.Family;
import rustyconnector.generic.lib.generic.server.Server;
import rustyconnector.generic.lib.generic.whitelist.Whitelist;
import rustyconnector.generic.lib.generic.whitelist.WhitelistPlayer;

import java.awt.print.Paper;
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

    public Map<ServerInfo,PaperServer> getRegisteredServers() {
        return this.registeredServers;
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
}
