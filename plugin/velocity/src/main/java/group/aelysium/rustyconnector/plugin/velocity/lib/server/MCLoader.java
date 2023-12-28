package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.velocity.events.mc_loader.RegisterEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.mc_loader.UnregisterEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.rmi.ConnectException;
import java.security.InvalidAlgorithmParameterException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MCLoader implements group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader {
    private final UUID id = UUID.randomUUID();
    private RegisteredServer registeredServer = null;
    private final ServerInfo serverInfo;
    private Family family;
    private int playerCount = 0;
    private int weight;
    private int softPlayerCap;
    private int hardPlayerCap;
    private AtomicInteger timeout;

    public MCLoader(ServerInfo serverInfo, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
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

    public UUID id() {
        return this.id;
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
    public void register(@NotNull String familyName) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        try {
            this.family = (Family) new Family.Reference(familyName).get();
        } catch (Exception ignore) {
            throw new InvalidAlgorithmParameterException("A family with the id `"+familyName+"` doesn't exist!");
        }

        try {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                ProxyLang.REGISTRATION_REQUEST.send(logger, serverInfo, family.id());

            if(api.services().server().contains(serverInfo)) throw new DuplicateRequestException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") can't be registered twice!");

            this.registeredServer = api.velocityServer().registerServer(serverInfo);
            if(this.registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            api.services().server().add(this);
            family.addServer(this);
        } catch (Exception error) {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                ProxyLang.ERROR.send(logger, serverInfo, family.id());
            throw new Exception(error.getMessage());
        }

        EventDispatch.Safe.fireAndForget(new RegisterEvent(family, this));
    }

    public void unregister(boolean removeFromFamily) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        try {
            MCLoader server = (MCLoader) new MCLoader.Reference(serverInfo).get();

            if (logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                ProxyLang.UNREGISTRATION_REQUEST.send(logger, serverInfo, family.id());

            Family family = server.family();

            api.velocityServer().unregisterServer(server.serverInfo());
            api.services().server().remove(this);
            if (removeFromFamily)
                family.removeServer(server);

            EventDispatch.Safe.fireAndForget(new UnregisterEvent(family, server));
        } catch (NullPointerException e) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                ProxyLang.ERROR.send(logger, serverInfo, family.id());
            throw new NullPointerException(e.getMessage());
        } catch (NoSuchElementException ignore) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                ProxyLang.ERROR.send(logger, serverInfo, family.id());
            throw new NullPointerException("Server ["+serverInfo.getName()+"]("+serverInfo.getAddress()+":"+serverInfo.getAddress().getPort()+") doesn't exist! It can't be unregistered!");
        } catch (Exception e) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                ProxyLang.ERROR.send(logger, serverInfo, family.id());
            throw new Exception(e);
        }
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
                Permission.constructNode("rustyconnector.<family id>.hardCapBypass",this.family.id())
        )) return true; // If the player has permission to bypass hard-player-cap, let them in.

        if(this.maxed()) return false; // If the player count is at hard-player-cap. Boot the player.

        if(Permission.validate(
                player,
                "rustyconnector.softCapBypass",
                Permission.constructNode("rustyconnector.<family id>.softCapBypass",this.family.id())
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

    public void playerLeft() {
        if(this.playerCount > 0) this.playerCount -= 1;
    }

    public void playerJoined() {
        this.playerCount += 1;
    }

    public double sortIndex() {
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
    public Family family() throws IllegalStateException, NullPointerException {
        if(this.registeredServer == null) throw new IllegalStateException("This server must be registered before you can find its family!");

        Family family;
        try {
            family = (Family) new Family.Reference(this.family.id()).get();
        } catch (Exception ignore) {
            throw new NullPointerException("A family with the id `"+this.family.id()+"` doesn't exist!");
        }

        return family;
    }

    public boolean connect(Player player) throws ConnectException {
        try {
            PartyService partyService = Tinder.get().services().party().orElseThrow();
            Party party = partyService.find(player).orElseThrow();

            try {
                if(partyService.settings().onlyLeaderCanSwitchServers())
                    if(!party.leader().equals(player)) {
                        player.sendMessage(ProxyLang.PARTY_ONLY_LEADER_CAN_SWITCH);
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
    public boolean directConnect(PlayerChooseInitialServerEvent event) {
        try {
            event.setInitialServer(this.registeredServer());
            return true;
        } catch(Exception ignore) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "["+this.serverInfo().getName()+"]" +
               "("+this.serverInfo().getAddress().getHostName()+":"+this.serverInfo().getAddress().getPort()+") - " +
               "["+this.playerCount()+" ("+this.softPlayerCap()+" <> "+this.softPlayerCap()+") w-"+this.weight()+"]" +
               "{"+ this.family.id() +"}";
    }

    @Override
    public int hashCode() {
        return this.serverInfo.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MCLoader mcLoader = (MCLoader) o;
        return Objects.equals(serverInfo, mcLoader.serverInfo);
    }
}
