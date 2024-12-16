package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.server.ServerAdapter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class FabricServerAdapter extends ServerAdapter {
    private final MinecraftServer server;

    public FabricServerAdapter(@NotNull MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void setMaxPlayers(int max) {

    }

    @Override
    public int onlinePlayerCount() {
        return this.server.getCurrentPlayerCount();
    }

    @Override
    public Optional<UUID> playerUUID(@NotNull String username) {
        ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(username);
        if(player == null) return Optional.empty();
        return Optional.of(player.getUuid());
    }

    @Override
    public Optional<String> playerUsername(@NotNull UUID uuid) {
        ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(uuid);
        if(player == null) return Optional.empty();
        return Optional.of(player.getName().toString());
    }

    @Override
    public boolean isOnline(@NotNull UUID uuid) {
        ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(uuid);
        if(player == null) return false;
        if(player.isDisconnected()) return false;
        return true;
    }

    @Override
    public void messagePlayer(@NotNull UUID uuid, @NotNull Component message) {
        try {
            ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(uuid);
            if(player == null) return;
            if(player.isDisconnected()) return;

            ((Audience) player).sendMessage(message);
        } catch (Exception e) {
            RC.Error(Error.from(e));
        }
    }

    @Override
    public void log(@NotNull Component message) {
        ((Audience) this.server).sendMessage(message);
    }
}
