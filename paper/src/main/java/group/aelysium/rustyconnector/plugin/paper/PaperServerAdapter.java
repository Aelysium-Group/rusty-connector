package group.aelysium.rustyconnector.plugin.paper;


import group.aelysium.rustyconnector.server.ServerAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
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
    public void setMaxPlayers(int i) {

    }

    @Override
    public int onlinePlayerCount() {
        return 0;
    }

    @Override
    public Optional<UUID> playerUUID(@NotNull String s) {
        return Optional.empty();
    }

    @Override
    public String playerUsername(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public boolean isOnline(@NotNull UUID uuid) {
        return false;
    }

    @Override
    public void sendMessage(UUID uuid, Component component) {

    }

    @Override
    public void log(Component component) {

    }
}
