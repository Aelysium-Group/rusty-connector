package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PartyConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.events.mc_loader.MCLoaderRegisterEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.mc_loader.MCLoaderUnregisterEvent;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.parties.ServerOverflowHandler;
import group.aelysium.rustyconnector.toolkit.velocity.parties.SwitchPower;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MCLoader implements IMCLoader {
    private final UUID uuid;
    private final String displayName;
    private final InetSocketAddress address;
    private RegisteredServer registeredServer = null;
    private Family family;
    private AtomicInteger playerCount = new AtomicInteger(0);
    private int weight;
    private int softPlayerCap;
    private int hardPlayerCap;
    private AtomicInteger timeout;

    public MCLoader(@NotNull UUID uuid, @NotNull InetSocketAddress address, String displayName, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
        this.uuid = uuid;
        this.address = address;
        this.displayName = displayName;

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

    public UUID uuid() {
        return this.uuid;
    }

    public String uuidOrDisplayName() {
        if(displayName == null) return this.uuid.toString();
        return this.displayName;
    }

    public int decreaseTimeout(int amount) {
        if(amount > 0) amount = amount * -1;
        this.timeout.addAndGet(amount);
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

    public ServerInfo serverInfo() { return new ServerInfo(this.uuid.toString(), this.address); }

    /**
     * Set the registered server associated with this PlayerServer.
     * @param registeredServer The RegisteredServer
     * @deprecated This method should never be used in production code! Use {@link MCLoader#register(String)} instead! This is only meant for code testing.
     */
    @Deprecated
    public void registeredServer(RegisteredServer registeredServer) {
        this.registeredServer = registeredServer;
    }

    public void register(@NotNull String familyId) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        try {
            this.family = new Family.Reference(familyId).get();
        } catch (Exception ignore) {
            throw new InvalidAlgorithmParameterException("A family with the id `"+familyId+"` doesn't exist!");
        }

        try {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                ProxyLang.REGISTRATION_REQUEST.send(logger, uuidOrDisplayName(), family.id());

            if(api.services().server().contains(this.uuid)) throw new RuntimeException("Server "+this.uuid+" can't be registered twice!");

            this.registeredServer = api.velocityServer().registerServer(serverInfo());
            if(this.registeredServer == null) throw new NullPointerException("Unable to register the server to the proxy.");

            api.services().server().add(this);
            family.addServer(this);
        } catch (Exception error) {
            if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
                ProxyLang.ERROR.send(logger, uuidOrDisplayName(), family.id());
            throw new Exception(error.getMessage());
        }

        EventDispatch.UnSafe.fireAndForget(new MCLoaderRegisterEvent(family, this));
    }

    public void unregister(boolean removeFromFamily) throws Exception {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        try {
            MCLoader server = new MCLoader.Reference(this.uuid).get();

            if (logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                ProxyLang.UNREGISTRATION_REQUEST.send(logger, uuidOrDisplayName(), family.id());

            Family family = server.family();

            api.velocityServer().unregisterServer(server.serverInfo());
            api.services().server().remove(this);

            try {
                Packet packet = api.services().packetBuilder().newBuilder()
                        .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_STALE_PING)
                        .sendingToMCLoader(server.uuid())
                        .build();
                api.services().magicLink().connection().orElseThrow().publish(packet);
            } catch (Exception ignore) {}

            if (removeFromFamily)
                family.removeServer(server);

            EventDispatch.UnSafe.fireAndForget(new MCLoaderUnregisterEvent(family, server));
        } catch (Exception e) {
            if(logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
                ProxyLang.ERROR.send(logger, uuidOrDisplayName(), family.id());
            throw new Exception(e);
        }
    }

    /**
     * Is the server full? Will return `true` if and only if `soft-player-cap` has been reached or surpassed.
     * @return `true` if the server is full
     */
    public boolean full() {
        return this.playerCount.get() >= softPlayerCap;
    }

    /**
     * Is the server maxed out? Will return `true` if and only if `hard-player-cap` has been reached or surpassed.
     * @return `true` if the server is maxed out
     */
    public boolean maxed() {
        return this.playerCount.get() >= hardPlayerCap;
    }

    @Override
    public int playerCount() {
        return this.playerCount.get();
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount.set(playerCount);
    }

    public double sortIndex() {
        return this.playerCount.get();
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
            family = new Family.Reference(this.family.id()).get();
        } catch (Exception ignore) {
            throw new NullPointerException("A family with the id `"+this.family.id()+"` doesn't exist!");
        }

        return family;
    }

    protected boolean validatePlayerLimits(com.velocitypowered.api.proxy.Player player) {
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

    private PlayerConnectable.Request internalConnect(IPlayer player) {
        CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
        PlayerConnectable.Request request = new PlayerConnectable.Request(player, result);

        try {
            if (!player.online()) {
                result.complete(ConnectionResult.failed(Component.text(player.username() + " isn't online.")));
                return request;
            }

            com.velocitypowered.api.proxy.Player velocityPlayer = player.resolve().orElse(null);
            if (velocityPlayer == null) {
                result.complete(ConnectionResult.failed(Component.text(player.username() + " couldn't be found.")));
                return request;
            }

            if (!this.validatePlayerLimits(velocityPlayer)) {
                result.complete(ConnectionResult.failed(Component.text("The server is currently full. Try again later.")));
                return request;
            }

            ConnectionRequestBuilder connection = player.resolve().orElseThrow().createConnectionRequest(this.registeredServer());
            try {
                ConnectionRequestBuilder.Result connectionResult = connection.connect().orTimeout(5, TimeUnit.SECONDS).get();

                if (!connectionResult.isSuccessful()) throw new NoOutputException();

                this.playerCount.incrementAndGet();
                result.complete(ConnectionResult.success(Component.text("You successfully connected to the server!"), this));
                return request;
            } catch (Exception ignore) {}
        } catch (Exception ignore) {}

        result.complete(ConnectionResult.failed(Component.text("Unable to connect you to the server!")));
        return request;
    }

    public PlayerConnectable.Request connect(IPlayer player) {
        // Handle party
        try {
            CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
            PlayerConnectable.Request request = new PlayerConnectable.Request(player, result);

            IParty party = Party.locate(player).orElseThrow();
            if(!Party.allowedToInitiateConnection(party, player)) {
                result.complete(ConnectionResult.failed(ProxyLang.PARTY_ONLY_LEADER_CAN_SWITCH));
                return request;
            }

            ServerOverflowHandler overflowHandler = Tinder.get().services().party().orElseThrow().settings().overflowHandler();
            if(overflowHandler == ServerOverflowHandler.HARD_BLOCK) {
                SwitchPower switchPower = Tinder.get().services().party().orElseThrow().settings().switchPower();
                int partyCount = party.players().size();
                int playerCap = 0;

                if(switchPower == SwitchPower.MINIMAL) playerCap = this.softPlayerCap();
                if(switchPower == SwitchPower.MODERATE) playerCap = this.hardPlayerCap();
                if(switchPower == SwitchPower.AGGRESSIVE) playerCap = Integer.MAX_VALUE;

                if((this.playerCount.get() + partyCount) > playerCap) {
                    result.complete(ConnectionResult.failed(Component.text("There isn't enough room in the server for your party!")));
                    return request;
                }
            }

            ConnectionResult partyResult = this.connect(party).result().get(30, TimeUnit.SECONDS);
            if(partyResult.connected())
                result.complete(ConnectionResult.success(Component.text("Successfully connected player and the player's party to the server!"), this));
            else
                result.complete(ConnectionResult.success(Component.text("Failed to connect the player to the server!"), this));
            return request;
        } catch (Exception ignore) {}

        return this.internalConnect(player);
    }

    public PartyConnectable.Request connect(IParty party) {
        CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
        PartyConnectable.Request request = new PartyConnectable.Request(party, result);

        SwitchPower switchPower = Tinder.get().services().party().orElseThrow().settings().switchPower();
        ServerOverflowHandler overflowHandler = Tinder.get().services().party().orElseThrow().settings().overflowHandler();

        Tinder.get().services().party().orElseThrow().queueConnector(() -> {
            AtomicInteger completeConnections = new AtomicInteger();

            party.players().forEach(player -> {
                boolean shouldKick = false;
                boolean shouldDitch = false;
                try {
                    switch (switchPower) {
                        case MINIMAL -> {
                            if(!this.full()) break;

                            if (overflowHandler == ServerOverflowHandler.STAY_BEHIND) shouldDitch = true;

                            if (overflowHandler == ServerOverflowHandler.KICK_FROM_PARTY) shouldKick = true;
                        }
                        case MODERATE -> {
                            if(!this.maxed()) break;

                            if (overflowHandler == ServerOverflowHandler.STAY_BEHIND) shouldDitch = true;

                            if (overflowHandler == ServerOverflowHandler.KICK_FROM_PARTY) shouldKick = true;
                        }
                        case AGGRESSIVE -> {}
                    }

                    if(shouldKick) {
                        party.leave(player);
                        player.sendMessage(ProxyLang.PARTY_FOLLOWING_KICKED_SERVER_FULL);
                        return;
                    }
                    if(shouldDitch) {
                        player.sendMessage(ProxyLang.PARTY_FOLLOWING_FAILED_SERVER_FULL);
                        return;
                    }

                    ConnectionResult playerResult = this.internalConnect(player).result().get(5, TimeUnit.SECONDS);
                    if (!playerResult.connected()) throw new NoOutputException();

                    completeConnections.getAndIncrement();
                } catch (Exception e) {
                    player.sendMessage(ProxyLang.PARTY_FOLLOWING_FAILED_GENERIC);

                    if (overflowHandler == ServerOverflowHandler.KICK_FROM_PARTY) party.leave(player);
                }
            });
            boolean consideredSuccessful = completeConnections.get() >= 1;

            if(consideredSuccessful) {
                result.complete(ConnectionResult.success(Component.text("Connected party to server successfully!"), this));
                ((Party) party).setServer(this);
            } else
                result.complete(ConnectionResult.failed(Component.text("Unable to connect your party to this server!")));
        });

        return request;
    }

    @Override
    public void leave(IPlayer player) {
        this.playerCount.decrementAndGet();
    }

    public void lock() {
        this.family().loadBalancer().lock(this);
    }

    public void unlock() {
        this.family().loadBalancer().unlock(this);
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
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MCLoader mcLoader = (MCLoader) o;
        return Objects.equals(uuid, mcLoader.uuid());
    }
}
