package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.core.lib.generic.database.MessageProcessor;
import group.aelysium.rustyconnector.core.lib.generic.database.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.generic.server.Server;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PaperServer implements Server {
    private static Map<RedisMessageType, MessageProcessor> messageProcessors = new HashMap<>();
    private RegisteredServer rawServer;
    private ServerInfo serverNode;
    private int playerCount = 0;
    private int priorityIndex = 0;
    private int softPlayerCap = 20;
    private int hardPlayerCap = 30;

    public PaperServer(ServerInfo serverNode, RegisteredServer rawServer, int softPlayerCap, int hardPlayerCap, int priorityIndex) {
        this.serverNode = serverNode;
        this.rawServer = rawServer;

        this.priorityIndex = priorityIndex;

        this.softPlayerCap = softPlayerCap;
        this.hardPlayerCap = hardPlayerCap;

        // Soft player cap MUST be at most the same value as hard player cap.
        if(this.softPlayerCap > this.hardPlayerCap) this.softPlayerCap = this.hardPlayerCap;
    }

    public RegisteredServer getRawServer() { return this.rawServer; }

    /**
     * Is the server full? Will return `true` if and only if `soft-player-cap` has been reached or surpassed.
     * @return `true` if the server is full
     */
    public boolean isFull() {
        return this.playerCount > softPlayerCap;
    }

    /**
     * Is the server maxed out? Will return `true` if and only if `hard-player-cap` has been reached or surpassed.
     * @return `true` if the server is maxed out
     */
    public boolean isMaxed() {
        return this.playerCount > hardPlayerCap;
    }

    @Override
    public int getPlayerCount() {
        return this.playerCount;
    }

    @Override
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    @Override
    public int getPriorityIndex() {
        return this.priorityIndex;
    }

    @Override
    public int getSoftPlayerCap() {
        return this.softPlayerCap;
    }

    @Override
    public int getHardPlayerCap() {
        return this.hardPlayerCap;
    }

    public static MessageProcessor getProcessor(RedisMessageType name) {
        return messageProcessors.get(name);
    }

    public static void registerProcessors() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        /*
         * Processes a request to register a server to the proxy
         */
        messageProcessors.put(RedisMessageType.REG, message -> {
            String familyName = message.getParameter("family");

            ServerFamily familyResponse = plugin.getProxy().getRegisteredFamilies().stream()
                    .filter(family ->
                            Objects.equals(family.getName(), familyName)
                    ).findFirst().orElse(null);
            if (familyResponse == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            InetSocketAddress address = message.getAddress();

            String serverName = message.getParameter("name");

            ServerInfo serverInfo = new ServerInfo(
                    serverName,
                    address
            );

            RegisteredServer registeredServer = plugin.getVelocityServer().registerServer(serverInfo);
            PaperServer server = new PaperServer(
                    serverInfo,
                    registeredServer,
                    Integer.parseInt(message.getParameter("soft-cap")),
                    Integer.parseInt(message.getParameter("hard-cap")),
                    Integer.parseInt(message.getParameter("priority"))
            );
            familyResponse.registerServer(server);

            server.setPlayerCount(Integer.parseInt(message.getParameter("player-count")));


        });

        /*
         * Processes a request to unregister a server from the proxy
         */
        messageProcessors.put(RedisMessageType.UNREG, message -> {
            String familyName = message.getParameter("family");

            ServerFamily familyResponse = plugin.getProxy().getRegisteredFamilies().stream()
                    .filter(family ->
                            Objects.equals(family.getName(), familyName)
                    ).findFirst().orElse(null);
            if (familyResponse == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

            InetSocketAddress address = message.getAddress();

            ServerInfo serverInfo = new ServerInfo(
                    message.getParameter("name"),
                    address
            );

            if(!familyResponse.containsServer(serverInfo)) throw new InvalidAlgorithmParameterException("The server requesting to un-register isn't on this server!");

            familyResponse.unregisterServer(serverInfo);
        });
    }
}
