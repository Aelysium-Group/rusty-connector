package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.generic.database.MessageProcessor;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.generic.util.logger.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.core.lib.generic.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.core.lib.generic.server.Family;
import group.aelysium.rustyconnector.core.lib.generic.server.Server;
import group.aelysium.rustyconnector.core.lib.generic.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.generic.firewall.WhitelistPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.Algorithm;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.security.InvalidAlgorithmParameterException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerFamily implements Family {
    private static final Map<RedisMessageType, MessageProcessor> messageProcessors = new HashMap<>();
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

    public static ServerFamily findFamily(String name) {
        return VelocityRustyConnector.getInstance().getProxy().getRegisteredFamilies().stream()
                .filter(family ->
                        Objects.equals(family.getName(), name)
                ).findFirst().orElse(null);
    }

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
            //player.createConnectionRequest(server.getRawServer()).connect();
            ConnectionRequestBuilder connection = player.createConnectionRequest(server.getRawServer());
            connection.connect().whenCompleteAsync((status, throwable) -> {
                VelocityRustyConnector.getInstance().logger().log("connected!!!!!");
                VelocityRustyConnector.getInstance().logger().error("",throwable);
            });
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
    public void registerServer(Server server) throws DuplicateRequestException {
        PaperServer paperServer = (PaperServer) server;
        InetSocketAddress address = paperServer.getRawServer().getServerInfo().getAddress();

        ServerInfo serverInfo = ((PaperServer) server).getRawServer().getServerInfo();

        if(this.containsServer(serverInfo)) {
            if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.REGISTRATION_REQUEST))
                VelocityRustyConnector.getInstance().logger().log(
                        "["+serverInfo.getName()+"]" +
                        "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                        " "+ Lang.getDynamic("canceled_icon") +" "+this.getName()
                );

            throw new DuplicateRequestException("Server ["+serverInfo.getName()+"]("+address.getAddress()+":"+address.getPort()+") can't be registered twice!");
        }

        this.registeredServers.add(paperServer);
        RegisteredServer registeredServer = VelocityRustyConnector.getInstance().getVelocityServer().registerServer(paperServer.getRawServer().getServerInfo());


        if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.REGISTRATION_REQUEST))
            VelocityRustyConnector.getInstance().logger().log(
                    "["+registeredServer.getServerInfo().getName()+"]" +
                    "("+registeredServer.getServerInfo().getAddress().getHostName()+":"+registeredServer.getServerInfo().getAddress().getPort()+")" +
                    " "+ Lang.getDynamic("registered_icon") +" "+this.getName()
            );
    }

    /**
     * Takes all servers in this family and runs them through this family's load balancer.
     * Re-arranging them in priority order.
     */
    public void balance() {
        Algorithm.getAlgorithm(this.algorithm).balance(this);
    }

    /**
     * Unregisters a server from this family.
     * @param serverInfo The info matching the server to unregister
     */
    public void unregisterServer(ServerInfo serverInfo) {
        PaperServer server = this.getServer(serverInfo);
        if(server == null) {
            if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.UNREGISTRATION_REQUEST))
                VelocityRustyConnector.getInstance().logger().log(
                        "["+serverInfo.getName()+"]" +
                                "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                                " "+ Lang.getDynamic("canceled_icon") +" "+this.getName()
                );

            throw new NullPointerException("The server requesting to un-register doesn't exist on this family!");
        }

        this.registeredServers.remove(server);
        VelocityRustyConnector.getInstance().getVelocityServer().unregisterServer(serverInfo);


        if(VelocityRustyConnector.getInstance().logger().getGate().check(GateKey.UNREGISTRATION_REQUEST))
            VelocityRustyConnector.getInstance().logger().log(
                    "["+serverInfo.getName()+"]" +
                            "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                            " "+ Lang.getDynamic("unregistered_icon") +" "+this.getName()
            );
    }

    /**
     * Gets a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     */
    public PaperServer getServer(ServerInfo serverInfo) throws NullPointerException {
        return this.getRegisteredServers().stream()
                .filter(server ->
                        Objects.equals(server.getRawServer().getServerInfo(), serverInfo)
                ).findFirst().orElse(null);
    }

    public void printInfo() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        Lang.print(plugin.logger(), Lang.get("info"));
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("Family Info");
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("   ---| Name: "+this.getName());
        plugin.logger().log("   ---| Online Players: "+this.getPlayerCount());
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
        plugin.logger().log("Servers are listed, from top to bottom, by priority. The server at the top will have players added to it first.");
        plugin.logger().log("The order of the servers will change over time as players join or leave them and their priority changes.");
        plugin.logger().log("Registered Servers: "+this.serverCount());
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
        plugin.logger().log(Lang.spacing());
        this.registeredServers.forEach(entry -> {
            plugin.logger().log("   ---| "+entry.getRawServer().getServerInfo().getName());
        });
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
    }

    public static MessageProcessor getProcessor(RedisMessageType name) {
        return messageProcessors.get(name);
    }

    public static void registerProcessors() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        /*
         * Sends a player to a family
         */
        messageProcessors.put(RedisMessageType.SEND, message -> {
            String familyName = message.getParameter("family");
            UUID uuid = UUID.fromString(message.getParameter("uuid"));

            ServerFamily familyResponse = plugin.getProxy().getRegisteredFamilies().stream()
                    .filter(family ->
                            Objects.equals(family.getName(), familyName)
                    ).findFirst().orElse(null);
            if (familyResponse == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            Player player = VelocityRustyConnector.getInstance().getVelocityServer().getPlayer(uuid).stream().findFirst().orElse(null);
            if(player == null) return;

            try {
                familyResponse.connect(player);
            } catch (MalformedURLException e) {
                player.disconnect(Component.text("Unable to connect you to the network! There are no default servers available!"));
                plugin.logger().log("There are no servers registered in the root family! Player's will be unable to join your network if there are no servers here!");
            }
        });

        /*
         * Processes a request to unregister a server from the proxy
         */
        messageProcessors.put(RedisMessageType.PLAYER_CNT, message -> {
            String familyName = message.getParameter("family-name");

            ServerFamily family = plugin.getProxy().findFamily(familyName);

            if (family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            InetSocketAddress address = message.getAddress();

            ServerInfo serverInfo = new ServerInfo(
                    message.getParameter("name"),
                    address
            );

            try {
                PaperServer server = family.getServer(serverInfo);

                family.setServerPlayerCount(Integer.parseInt(message.getParameter("player-count")), server);
            } catch (NullPointerException e) {
                throw new InvalidAlgorithmParameterException("The provided server doesn't exist in this family!");
            } catch (NumberFormatException e) {
                throw new InvalidAlgorithmParameterException("The player count provided wasn't valid!");
            }
        });
    }
    public static void unregisterProcessors() {
        ServerFamily.messageProcessors.clear();
    }
}
