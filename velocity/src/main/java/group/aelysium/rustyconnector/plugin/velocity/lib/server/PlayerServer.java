package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.security.InvalidAlgorithmParameterException;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerServer implements group.aelysium.rustyconnector.core.lib.model.PlayerServer {
    private RegisteredServer registeredServer = null;
    private final ServerInfo serverInfo;
    private String familyName;
    private int playerCount = 0;
    private int weight;
    private int softPlayerCap;
    private int hardPlayerCap;

    private AtomicInteger timeout;

    public PlayerServer(ServerInfo serverInfo, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
        this.serverInfo = serverInfo;

        this.weight = Math.max(weight, 0);

        this.softPlayerCap = softPlayerCap;
        this.hardPlayerCap = hardPlayerCap;

        // Soft player cap MUST be at most the same value as hard player cap.
        if(this.softPlayerCap > this.hardPlayerCap) this.softPlayerCap = this.hardPlayerCap;

        this.timeout = new AtomicInteger(timeout);
    }

    public boolean isStale() {
        return this.timeout.get() <= 0;
    }

    public void setTimeout(int newTimeout) {
        if(newTimeout < 0) throw new IndexOutOfBoundsException("New timeout must be at least 0!");
        this.timeout.set(newTimeout);
    }

    public int decreaseTimeout() {
        this.timeout.decrementAndGet();
        if(this.timeout.get() < 0) this.timeout.set(0);

        return this.timeout.get();
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
     * Set the registered server associated with this PlayerServer.
     * @param registeredServer The RegisteredServer
     * @deprecated This method should never be used in production code! Use `PlayerServer#register` instead! This is only meant for code testing.
     */
    @Deprecated
    public void setRegisteredServer(RegisteredServer registeredServer) {
        this.registeredServer = registeredServer;
    }

    /**
     * Registers a server to the proxy.
     * @param familyName The family to associate the server with.
     * @throws DuplicateRequestException If the server has already been registered to the proxy.
     * @throws InvalidAlgorithmParameterException If the family doesn't exist.
     */
    public void register(String familyName) throws Exception {
        VelocityAPI api = VelocityAPI.get();

        this.registeredServer = api.services().serverService().registerServer(this, familyName);

        this.familyName = familyName;
    }

    /**
     * Is the server full? Will return `true` if and only if `soft-player-cap` has been reached or surpassed.
     * @return `true` if the server is full
     */
    public boolean isFull() {
        return this.playerCount >= softPlayerCap;
    }

    /**
     * Is the server maxed out? Will return `true` if and only if `hard-player-cap` has been reached or surpassed.
     * @return `true` if the server is maxed out
     */
    public boolean isMaxed() {
        return this.playerCount >= hardPlayerCap;
    }


    /**
     * Validates the player against the server's current player count.
     * If the server is full or the player doesn't have permissions to bypass soft and hard player caps. They will be kicked
     * @param player The player to validate
     * @return `true` if the player is able to join. `false` otherwise.
     */
    public boolean validatePlayer(Player player) {
        if(Permission.validate(
                player,
                "rustyconnector.hardCapBypass",
                Permission.constructNode("rustyconnector.<family name>.hardCapBypass",this.familyName)
        )) return true; // If the player has permission to bypass hard-player-cap, let them in.

        if(this.isMaxed()) return false; // If the player count is at hard-player-cap. Boot the player.

        if(Permission.validate(
                player,
                "rustyconnector.softCapBypass",
                Permission.constructNode("rustyconnector.<family name>.softCapBypass",this.familyName)
        )) return true; // If the player has permission to bypass soft-player-cap, let them in.

        return !this.isFull();
    }

    @Override
    public int getPlayerCount() {
        //return 0;
        return this.playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
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
    public BaseServerFamily getFamily() throws IllegalStateException, NullPointerException {
        if(this.registeredServer == null) throw new IllegalStateException("This server must be registered before you can find its family!");
        VelocityAPI api = VelocityAPI.get();

        BaseServerFamily family = api.services().familyService().find(this.familyName);
        if(family == null) throw new NullPointerException("There is no family with that name!");

        return family;
    }

    /**
     * Connects a player to the server.
     * This also increases the player count on this server by 1.
     * @param player The player to connect.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     */
    public boolean connect(Player player) {
        VelocityAPI api = VelocityAPI.get();

        ConnectionRequestBuilder connection = player.createConnectionRequest(this.getRegisteredServer());
        try {
            if (!connection.connect().get().isSuccessful()) return false;
        } catch (Exception ignore) {
            return false;
        }

        try {
            PartyService partyService = api.services().partyService().orElse(null);
            if (partyService == null) throw new NoOutputException();

            Party party = partyService.find(player).orElse(null);
            if (party == null) throw new NoOutputException();

            this.connect(party);
        } catch (NoOutputException ignore) {
        } catch (Exception e) {
            api.logger().log("Issue trying to pull party with player! " + e.getMessage());
        }
        return true;
    }

    /**
     * Set's a connections initial server to the server.
     * @param event The connection to set.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     */
    public boolean connect(PlayerChooseInitialServerEvent event) {
        try {
            event.setInitialServer(this.getRegisteredServer());
            return true;
        } catch(Exception ignore) {
            return false;
        }
    }

    /**
     * Connects a party to the server.
     * This also increases the player count on this server by the number of players in the party.
     * If any members of the party have already connected to this server, they will be ignored.
     * @param party The party to connect.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     */
    public void connect(Party party) {
        party.setServer(this);

        party.players().forEach(player -> {
            try {
                ServerConnection serverConnection = player.getCurrentServer().orElse(null);
                if(serverConnection != null)
                    if(!serverConnection.getServer().equals(this.registeredServer)) return;

                ConnectionRequestBuilder connection = player.createConnectionRequest(this.getRegisteredServer());
                connection.connect();
            } catch (Exception e) {
                player.sendMessage(Component.text("There was an issue following your party! You've been kicked. "+e.getMessage(), NamedTextColor.RED));
                party.leave(player);
            }
        });
    }

    /**
     * Reduces the player count on this server by 1.
     */
    public void playerLeft() {
        if(this.playerCount > 0) this.playerCount -= 1;
    }

    /**
     * Increases the player count on this server by 1.
     */
    public void playerJoined() {
        this.playerCount += 1;
    }

    @Override
    public String toString() {
        return "["+this.getServerInfo().getName()+"]" +
               "("+this.getServerInfo().getAddress().getHostName()+":"+this.getServerInfo().getAddress().getPort()+") - " +
               "["+this.getPlayerCount()+" ("+this.getSoftPlayerCap()+" <> "+this.getSoftPlayerCap()+") w-"+this.getWeight()+"]" +
               "{"+ this.familyName +"}";
    }

    public boolean equals(PlayerServer server) {
        return this.serverInfo.equals(server.getServerInfo());
    }
}
