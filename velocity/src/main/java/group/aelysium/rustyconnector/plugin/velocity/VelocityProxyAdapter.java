package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.proxy.ProxyAdapter;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityProxyAdapter extends ProxyAdapter {
    private final ProxyServer velocity;
    private final PluginLogger logger;

    public VelocityProxyAdapter(@NotNull ProxyServer velocity, @NotNull PluginLogger logger) {
        this.velocity = velocity;
        this.logger = logger;
    }

    @Override
    public @Nullable Object convertToObject(@NotNull Player player) {
        return this.velocity.getPlayer(player.uuid()).orElse(null);
    }

    @Override
    public @NotNull Player convertToRCPlayer(@NotNull Object o) {
        if(!(o instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) throw new RuntimeException("Provided object was not a player!");
        return new Player(velocityPlayer.getUniqueId(), velocityPlayer.getUsername());
    }

    @Override
    public @NotNull String extractHostname(@NotNull Player player) {
        if(!(this.convertToObject(player) instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) throw new RuntimeException("Provided object was not a player!");
        return velocityPlayer.getVirtualHost().map(InetSocketAddress::getHostString).orElse("").toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean registerServer(@NotNull Server server) {
        ServerInfo info = new ServerInfo(server.id(), server.address());

        try {
            RegisteredServer registeredServer = this.velocity.registerServer(info);
            ServerPing ping = registeredServer.ping().get(10, TimeUnit.SECONDS);

            server.property("velocity_RegisteredServer", registeredServer);
            return true;
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To register the server: "+server.id()+" "+server.address()));
            try {
                this.velocity.unregisterServer(info);
            } catch (Exception ignore2) {}
            return false;
        }
    }

    @Override
    public void unregisterServer(@NotNull Server server) {
        this.velocity.unregisterServer(new ServerInfo(server.id(), server.address()));
    }

    @Override
    public boolean serverExists(@NotNull Server server) {
        return this.velocity.getServer(server.id()).isPresent();
    }

    @Override
    public void log(@NotNull Component component) {
        this.logger.send(component);
    }

    @Override
    public void messagePlayer(@NotNull UUID uuid, @NotNull Component component) {
        try {
            RC.P.Player(uuid).orElseThrow(()->new NullPointerException("No player with the uuid "+uuid+" is online.")).message(component);
        } catch (Exception e) {
            RC.Error(Error.from(e));
        }
    }

    @Override
    public Optional<Server> fetchServer(@NotNull Player player) {
        com.velocitypowered.api.proxy.Player velocityPlayer = null;
        try {
            velocityPlayer = this.velocity.getPlayer(player.uuid()).orElseThrow();
            try {
                velocityPlayer = this.velocity.getPlayer(player.username()).orElseThrow();
            } catch (Exception ignore) {}
        } catch (Exception ignore) {}
        if(velocityPlayer == null) return Optional.empty();

        if(velocityPlayer.getCurrentServer().isEmpty()) return Optional.empty();

        ServerConnection serverConnection = velocityPlayer.getCurrentServer().orElseThrow();

        return RC.P.Server(serverConnection.getServerInfo().getName());
    }

    @Override
    public void disconnect(@NotNull Player player, @NotNull Component component) {
        if(!(this.convertToObject(player) instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) throw new RuntimeException("Provided object was not a player!");
        velocityPlayer.disconnect(component);
    }

    @Override
    public boolean checkPermission(@NotNull Player player, @NotNull String permission) {
        if(!(this.convertToObject(player) instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) throw new RuntimeException("Provided object was not a player!");
        return velocityPlayer.hasPermission(permission);
    }

    @Override
    public Player.Connection.Request connectServer(@NotNull Server server, @NotNull Player player) {
        if(!(this.convertToObject(player) instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) throw new RuntimeException("Provided object was not a player!");

        RegisteredServer registeredServer = (RegisteredServer) server.property("velocity_RegisteredServer")
                .orElseThrow(()->new NoSuchElementException("The server "+server.id()+" doesn't seem to have a RegisteredServer (from velocity) associated with it."));

        ConnectionRequestBuilder connection = velocityPlayer.createConnectionRequest(registeredServer);
        try {
            ConnectionRequestBuilder.Result connectionResult = connection.connect().get(5, TimeUnit.SECONDS);

            if (!connectionResult.isSuccessful()) throw new Exception();

            server.setPlayerCount(server.players() + 1);
            return Player.Connection.Request.successfulRequest(player, Component.text("You successfully connected to the server!"), server);
        } catch (Exception ignore) {}
        return Player.Connection.Request.failedRequest(player, Component.text("There was an issue connecting!"));
    }
}
