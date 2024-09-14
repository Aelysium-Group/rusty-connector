package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.server.ServerAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PaperServerAdapter extends ServerAdapter {
    private final Server server;
    private final PluginLogger logger;

    public PaperServerAdapter(@NotNull Server server, @NotNull PluginLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public void setMaxPlayers(int max) {
        this.server.setMaxPlayers(max);
    }

    @Override
    public int onlinePlayerCount() {
        return this.server.getOnlinePlayers().size();
    }

    @Override
    public Optional<UUID> playerUUID(@NotNull String username) {
        Player player = this.server.getPlayer(username);
        if(player == null) return Optional.empty();
        return Optional.of(player.getUniqueId());
    }

    @Override
    public Optional<String> playerUsername(@NotNull UUID uuid) {
        Player player = this.server.getPlayer(uuid);
        if(player == null) return Optional.empty();
        return Optional.of(player.getName());
    }

    @Override
    public boolean isOnline(@NotNull UUID uuid) {
        Player player = this.server.getPlayer(uuid);
        if(player == null) return false;
        return player.isOnline();
    }

    @Override
    public void sendMessage(UUID uuid, Component message) {
        Player player = this.server.getPlayer(uuid);
        if(player == null) return;
        player.sendMessage(message);
    }

    @Override
    public void log(Component message) {
        this.logger.send(message);
    }
}
