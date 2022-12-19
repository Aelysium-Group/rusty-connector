package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.Redis;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.model.Server;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;

import java.security.InvalidAlgorithmParameterException;

public class PaperServer implements Server {
    private RegisteredServer registeredServer = null;
    private final ServerInfo serverInfo;
    private String familyName;
    private int playerCount = 0;
    private int weight = 0;
    private int softPlayerCap = 20;
    private int hardPlayerCap = 30;

    public PaperServer(ServerInfo serverInfo, int softPlayerCap, int hardPlayerCap, int weight) {
        this.serverInfo = serverInfo;

        this.weight = Math.max(weight, 0);

        this.softPlayerCap = softPlayerCap;
        this.hardPlayerCap = hardPlayerCap;

        // Soft player cap MUST be at most the same value as hard player cap.
        if(this.softPlayerCap > this.hardPlayerCap) this.softPlayerCap = this.hardPlayerCap;
    }

    public String getAddress() {
        return this.getServerInfo().getAddress().getHostName() + ":" + this.getServerInfo().getAddress().getPort();
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public RegisteredServer getRegisteredServer() {
        if(this.registeredServer == null) throw new IllegalStateException("This server must be registered before you can find its family!");
        return this.registeredServer;
    }

    public ServerInfo getServerInfo() { return this.serverInfo; }

    /**
     * Registers a server to the proxy.
     * @param familyName The family to associate the server with.
     * @throws DuplicateRequestException If the server has already been registered to the proxy.
     * @throws InvalidAlgorithmParameterException Of the family doesn't exist.
     */
    public void register(String familyName) throws Exception {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        this.registeredServer = plugin.getProxy().registerServer(this, familyName);

        this.familyName = familyName;
    }

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
        return 0;
        //return this.playerCount;
    }

    @Override
    public void setPlayerCount(int playerCount) {
        this.playerCount = 0;
        //this.playerCount = playerCount;
    }

    @Override
    public int getSortIndex() {
        return this.playerCount;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public int getSoftPlayerCap() {
        return this.softPlayerCap;
    }

    @Override
    public int getHardPlayerCap() {
        return this.hardPlayerCap;
    }

    /**
     * Get the family a server is associated with.
     * @return A Family.
     * @throws IllegalStateException If the server hasn't been registered yet.
     * @throws NullPointerException If the family associated with this server doesn't exist.
     */
    public ServerFamily<? extends PaperServerLoadBalancer> getFamily() throws IllegalStateException, NullPointerException {
        if(this.registeredServer == null) throw new IllegalStateException("This server must be registered before you can find its family!");
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        ServerFamily<? extends PaperServerLoadBalancer> family = plugin.getProxy().getFamilyManager().find(this.familyName);
        if(family == null) throw new NullPointerException("There is no family with that name!");

        return family;
    }

    /**
     * Sends a ping to the specific server.
     * @param redis The redis connection to use.
     */
    public void ping(Redis redis, String privateKey) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        RedisMessage message = new RedisMessage(
                privateKey,
                RedisMessageType.PING,
                this.getAddress()
        );

        message.dispatchMessage(redis);

        if(plugin.logger().getGate().check(GateKey.PING))
            VelocityLang.PING.send(plugin.logger(),this.serverInfo);
    }

    /**
     * Connects a player to the server.
     * This also increases the player count on this server by 1.
     * @param player The player to connect.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     */
    public boolean connect(Player player) {
        ConnectionRequestBuilder connection = player.createConnectionRequest(this.getRegisteredServer());
        try {
            boolean didSucceed = connection.connect().get().isSuccessful();
            //if(didSucceed) this.playerCount += 1;
            return didSucceed;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reduces the player count on this server by 1.
     */
    public void playerLeft() {
        //if(this.playerCount > 0) this.playerCount -= 1;
    }

    @Override
    public String toString() {
        return "["+this.getServerInfo().getName()+"]" +
               "("+this.getServerInfo().getAddress().getHostName()+":"+this.getServerInfo().getAddress().getPort()+") - " +
               "["+this.getPlayerCount()+" ("+this.getSoftPlayerCap()+" <> "+this.getSoftPlayerCap()+") w-"+this.getWeight()+"]" +
               "{"+ this.familyName +"}";
    }
}
