package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.proxy.ProxyAdapter;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Optional;
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
    public void registerServer(@NotNull Server server) {
        this.velocity.registerServer(new ServerInfo(server.uuid().toString(), server.address()));
    }

    @Override
    public void unregisterServer(@NotNull Server server) {
        this.velocity.unregisterServer(new ServerInfo(server.uuid().toString(), server.address()));
    }

    @Override
    public void log(@NotNull Component component) {
        this.logger.send(component);
    }

    @Override
    public void messagePlayer(@NotNull Player player, @NotNull Component component) {
        if(!(this.convertToObject(player) instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) throw new RuntimeException("Provided object was not a player!");
        velocityPlayer.sendMessage(component);
    }

    @Override
    public Optional<Server> fetchServer(@NotNull Player player) {
        return Optional.empty();
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
        if(!(server.raw() instanceof RegisteredServer registeredServer)) throw new RuntimeException("Server basis in not RegisteredServer!");

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
