package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.rmi.ConnectException;
import java.security.InvalidAlgorithmParameterException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerServer implements group.aelysium.rustyconnector.core.lib.model.PlayerServer {
    private RegisteredServer registeredServer = null;
    private final ServerInfo serverInfo;
    private BaseServerFamily family;
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

    public boolean stale() {
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

    public String address() {
        return this.serverInfo().getAddress().getHostName() + ":" + this.serverInfo().getAddress().getPort();
    }

    public RegisteredServer registeredServer() {
        if(this.registeredServer == null) throw new IllegalStateException("This server must be registered before you can find its family!");
        return this.registeredServer;
    }

    public ServerInfo serverInfo() { return this.serverInfo; }

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

        BaseServerFamily family = api.services().familyService().find(familyName);
        if(family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+familyName+"` doesn't exist!");

        this.registeredServer = api.services().serverService().registerServer(this, family);

        this.family = family;
    }

    /**
     * Is the server full? Will return `true` if and only if `soft-player-cap` has been reached or surpassed.
     * @return `true` if the server is full
     */
    public boolean full() {
        return this.playerCount >= softPlayerCap;
    }

    /**
     * Is the server maxed out? Will return `true` if and only if `hard-player-cap` has been reached or surpassed.
     * @return `true` if the server is maxed out
     */
    public boolean maxed() {
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
                Permission.constructNode("rustyconnector.<family name>.hardCapBypass",this.family.name())
        )) return true; // If the player has permission to bypass hard-player-cap, let them in.

        if(this.maxed()) return false; // If the player count is at hard-player-cap. Boot the player.

        if(Permission.validate(
                player,
                "rustyconnector.softCapBypass",
                Permission.constructNode("rustyconnector.<family name>.softCapBypass",this.family.name())
        )) return true; // If the player has permission to bypass soft-player-cap, let them in.

        return !this.full();
    }

    @Override
    public int playerCount() {
        //return 0;
        return this.playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    @Override
    public int sortIndex() {
        return this.playerCount;
    }

    @Override
    public int weight() {
        return this.weight;
    }

    @Override
    public int softPlayerCap() {
        return this.softPlayerCap;
    }

    @Override
    public int hardPlayerCap() {
        return this.hardPlayerCap;
    }

    /**
     * Get the family a server is associated with.
     * @return A Family.
     * @throws IllegalStateException If the server hasn't been registered yet.
     * @throws NullPointerException If the family associated with this server doesn't exist.
     */
    public BaseServerFamily family() throws IllegalStateException, NullPointerException {
        if(this.registeredServer == null) throw new IllegalStateException("This server must be registered before you can find its family!");
        VelocityAPI api = VelocityAPI.get();

        BaseServerFamily family = api.services().familyService().find(this.family.name());
        if(family == null) throw new NullPointerException("There is no family with that name!");

        return family;
    }

    public boolean connect(Player player) throws ConnectException {
        try {
            PartyService partyService = VelocityAPI.get().services().partyService().orElseThrow();
            Party party = partyService.find(player).orElseThrow();

            try {
                if(partyService.settings().onlyLeaderCanSwitchServers())
                    if(!party.leader().equals(player)) {
                        player.sendMessage(Component.text("Only the party leader can switch servers!", NamedTextColor.RED));
                        return false;
                    }

                party.connect(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ignore) {}

        return directConnect(player);
    }

    public boolean directConnect(Player player) throws ConnectException {
        ConnectionRequestBuilder connection = player.createConnectionRequest(this.registeredServer());
        try {
            ConnectionRequestBuilder.Result result = connection.connect().orTimeout(5, TimeUnit.SECONDS).get();

            if(result.isSuccessful()) {
                this.playerJoined();
                return true;
            }
        } catch (Exception e) {
            throw new ConnectException("Unable to connect to that server!", e);
        }

        return false;
    }

    /**
     * Set's a connections initial server to the server.
     * @param event The connection to set.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     */
    public boolean connect(PlayerChooseInitialServerEvent event) {
        try {
            event.setInitialServer(this.registeredServer());
            return true;
        } catch(Exception ignore) {
            return false;
        }
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
        return "["+this.serverInfo().getName()+"]" +
               "("+this.serverInfo().getAddress().getHostName()+":"+this.serverInfo().getAddress().getPort()+") - " +
               "["+this.playerCount()+" ("+this.softPlayerCap()+" <> "+this.softPlayerCap()+") w-"+this.weight()+"]" +
               "{"+ this.family.name() +"}";
    }

    public boolean equals(PlayerServer server) {
        return this.serverInfo.equals(server.serverInfo());
    }
}
