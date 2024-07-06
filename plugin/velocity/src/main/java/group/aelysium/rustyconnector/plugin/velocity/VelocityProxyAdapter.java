package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.common.exception.NoOutputException;
import group.aelysium.rustyconnector.proxy.players.Player;
import group.aelysium.rustyconnector.toolkit.proxy.ProxyAdapter;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class VelocityProxyAdapter extends ProxyAdapter<com.velocitypowered.api.proxy.Player, RegisteredServer> {
    private final ProxyServer velocity;

    public VelocityProxyAdapter(ProxyServer velocity) {
        this.velocity = velocity;
    }

    @Override
    public @Nullable com.velocitypowered.api.proxy.Player convertToProxyPlayer(@NotNull IPlayer player) {
        return this.velocity.getPlayer(player.uuid()).orElse(null);
    }

    @Override
    public @NotNull IPlayer convertToRCPlayer(@NotNull com.velocitypowered.api.proxy.Player player) {
        return new Player(player.getUniqueId(), player.getUsername());
    }

    @Override
    public @NotNull String extractHostname(@NotNull IPlayer player) {
        return this.convertToProxyPlayer(player).getVirtualHost().map(InetSocketAddress::getHostString).orElse("").toLowerCase(Locale.ROOT);
    }

    @Override
    public void registerMCLoader(@NotNull MCLoader mcloader) {
        this.velocity.registerServer(new ServerInfo(mcloader.uuidOrDisplayName(), mcloader.address()));
    }

    @Override
    public void unregisterMCLoader(@NotNull MCLoader mcloader) {
        this.velocity.unregisterServer(new ServerInfo(mcloader.uuidOrDisplayName(), mcloader.address()));
    }

    @Override
    public void logComponent(@NotNull Component component) {

    }

    @Override
    public void messagePlayer(@NotNull IPlayer player, @NotNull Component message) {
        this.convertToProxyPlayer(player).sendMessage(message);
    }

    @Override
    public Optional<MCLoader> fetchMCLoader(@NotNull IPlayer player) {
        return Optional.empty();
    }

    @Override
    public void disconnect(@NotNull IPlayer player, @NotNull Component reason) {
        this.convertToProxyPlayer(player).disconnect(reason);
    }

    @Override
    public boolean checkPermission(@NotNull IPlayer player, @NotNull String permission) {
        return this.convertToProxyPlayer(player).hasPermission(permission);
    }

    @Override
    public IPlayer.Connection.Request connectServer(@NotNull MCLoader mcloader, @NotNull IPlayer player) {
        RegisteredServer server = (RegisteredServer) mcloader.raw();

        com.velocitypowered.api.proxy.Player velocityPlayer = this.convertToProxyPlayer(player);
        if(velocityPlayer == null) {
            return IPlayer.Connection.Request.failedRequest(player, Component.text("No player could be found!"));
        }

        ConnectionRequestBuilder connection = velocityPlayer.createConnectionRequest(server);
        try {
            ConnectionRequestBuilder.Result connectionResult = connection.connect().orTimeout(5, TimeUnit.SECONDS).get();

            if (!connectionResult.isSuccessful()) throw new NoOutputException();

            mcloader.setPlayerCount((int) (mcloader.players() + 1));
            return IPlayer.Connection.Request.successfulRequest(player, Component.text("You successfully connected to the server!"), mcloader);
        } catch (Exception ignore) {}
        return IPlayer.Connection.Request.failedRequest(player, Component.text("There was an issue connecting!"));
    }
}
