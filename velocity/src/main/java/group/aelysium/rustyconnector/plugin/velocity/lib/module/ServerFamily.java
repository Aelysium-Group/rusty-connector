package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.core.lib.util.logger.GateKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.core.lib.model.Family;
import group.aelysium.rustyconnector.core.lib.model.Server;
import group.aelysium.rustyconnector.core.lib.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.firewall.WhitelistPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.Algorithm;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerFamily implements Family {
    private final List<PaperServer> registeredServers = new ArrayList<>();
    private final String name;
    private final Whitelist whitelist;
    protected int playerCount = 0;
    protected AlgorithmType algorithm;
    public ServerFamily(String name, AlgorithmType algorithm, Whitelist whitelist) {
        this.name = name;
        this.algorithm = algorithm;
        this.whitelist = whitelist;
    }

    public String algorithm() { return this.algorithm.toString(); }
    public int serverCount() { return this.registeredServers.size(); }

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    public int getPlayerCount() {
        AtomicInteger newPlayerCount = new AtomicInteger();
        registeredServers.forEach(server -> {
            newPlayerCount.addAndGet(server.getPlayerCount());
        });

        playerCount = newPlayerCount.get();

        return this.playerCount;
    }

    /**
     * Set's the player count of a sub-server. Then re-calculates the aggregate player count of this family.
     * Finally, re-balance the family's priority order of sub-servers.
     * @param playerCount Player count to set.
     * @param server The server to set.
     */
    public void setServerPlayerCount(int playerCount, PaperServer server) {
        server.setPlayerCount(playerCount);

        this.getPlayerCount();

        this.balance();
    }

    public boolean containsServer(ServerInfo serverInfo) {
        return !(this.getServer(serverInfo) == null);
    }
    public boolean containsServer(String name) {
        return !(this.getServer(name) == null);
    }

    /**
     * Connect a player to this family
     * @param player The player to connect
     */
    public void connect(Player player) throws MalformedURLException {
        if(this.whitelist != null) {
            String ip = player.getRemoteAddress().getHostString();

            WhitelistPlayer whitelistPlayer = new WhitelistPlayer(player.getUsername(), player.getUniqueId(), ip);

            if (!whitelist.validate(whitelistPlayer))
                player.disconnect(Component.text("You aren't whitelisted on this server!"));
        }

        if(this.registeredServers.size() == 0) throw new MalformedURLException("There are no servers in this family!");

        PaperServer server = this.registeredServers.get(0); // Get the server that is currently listed as highest priority

        if(server.isFull())
            if(!player.hasPermission("rustyconnector.bypasssoftcap"))
                return;
        if(server.isMaxed())
            return;

        try {
            ConnectionRequestBuilder connection = player.createConnectionRequest(server.getRegisteredServer());
            connection.connect().whenCompleteAsync((status, throwable) -> {});
        } catch (Exception e) {
            VelocityRustyConnector.getInstance().logger().error("",e);
        }
    }

    public List<PaperServer> getRegisteredServers() {
        return this.registeredServers;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void registerServer(Server server) {
        this.registeredServers.add((PaperServer) server);
    }

    /**
     * Takes all servers in this family and runs them through this family's load balancer.
     * Re-arranging them in priority order.
     */
    public void balance() {
        if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.UNREGISTRATION_REQUEST))
            VelocityRustyConnector.getInstance().logger().log(
                    this.getName()+" "+Lang.get(LangKey.ICON_FAMILY_BALANCING)
            );
        Algorithm.getAlgorithm(this.algorithm).balance(this);
    }

    /**
     * Removes a server from this family.
     * @param server The server to remove.
     */
    public void removeServer(PaperServer server) {
        this.registeredServers.remove(server);
    }

    /**
     * Gets a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     */
    public PaperServer getServer(ServerInfo serverInfo) {
        return this.getServer(serverInfo.getName());
    }

    /**
     * Gets a server that is a part of the family.
     * @param name The name of the server.
     * @return A found server or `null` if there's no match.
     */
    public PaperServer getServer(String name) {
        return this.getRegisteredServers().stream()
                .filter(server -> Objects.equals(server.getServerInfo().getName(), name)
                ).findFirst().orElse(null);
    }

    /**
     * Gets a server that is a part of the family.
     * @param address The address of the server to get
     */
    public PaperServer getServer(InetSocketAddress address) throws NullPointerException {
        return this.getRegisteredServers().stream()
                .filter(server ->
                        Objects.equals(server.getRegisteredServer().getServerInfo().getAddress(), address)
                ).findFirst().orElse(null);
    }

    /**
     * Print info related to this family.
     */
    public void printInfo() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        (new LangMessage(plugin.logger()))
                .insert(Lang.info())
                .insert(Lang.spacing())
                .insert("Family Info")
                .insert(Lang.spacing())
                .insert("   ---| Name: "+this.getName())
                .insert("   ---| Online Players: "+this.getPlayerCount())
                .insert("   ---| Registered Servers: "+this.serverCount())
                .insert("   ---| Load Balancing Algorithm: "+this.algorithm())
                .insert(Lang.spacing())
                .insert(Lang.border())
                .print();
    }


    /**
     * Print info related to the servers on this server
     */
    public void printServers() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LangMessage langMessage = (new LangMessage(plugin.logger()))
                .insert(Lang.info())
                .insert(Lang.spacing())
                .insert("All servers registered to the family: "+this.name)
                .insert("Servers are listed, from top to bottom, by priority. The server at the top will have players added to it first.")
                .insert("The order of the servers will change over time as players join or leave them and their priority changes.")
                .insert("Registered Servers: "+this.serverCount())
                .insert("Online Players: "+this.serverCount())
                .insert(Lang.spacing())
                .insert(Lang.border())
                .insert(Lang.spacing());

        AtomicInteger index = new AtomicInteger(1);
        this.registeredServers.forEach(entry -> {

            // Looks like: ---| 1. [server name](127.0.0.1:25565) [0 (10 <> 20)]

            langMessage
                    .insert(
                            "   ---| "+index.get()+". ["+entry.getRegisteredServer().getServerInfo().getName()+"]" +
                            "("+ AddressUtil.addressToString(entry.getRegisteredServer().getServerInfo().getAddress()) +") " +
                            "["+entry.getPlayerCount()+" ("+entry.getSoftPlayerCap()+" <> "+entry.getSoftPlayerCap()+")"
                    );

            index.getAndIncrement();
        });
        langMessage
                .insert(Lang.spacing())
                .insert(Lang.border())
                .print();
    }
}
