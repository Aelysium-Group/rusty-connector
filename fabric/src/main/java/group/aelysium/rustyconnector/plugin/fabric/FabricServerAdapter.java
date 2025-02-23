package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.server.ServerAdapter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class FabricServerAdapter extends ServerAdapter {
    private final MinecraftServer server;
    private final CommandManager<?> commandManager;

    public FabricServerAdapter(@NotNull MinecraftServer server, @NotNull CommandManager<?> commandManager) {
        this.server = server;
        this.commandManager = commandManager;
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
    public void teleport(@NotNull UUID from, @NotNull UUID to) {
        try {
            ServerPlayerEntity player1 = server.getPlayerManager().getPlayer(from);
            if(player1 == null) throw new NullPointerException("Player "+from+" could not be found.");
            
            ServerPlayerEntity player2 = server.getPlayerManager().getPlayer(to);
            if(player2 == null) throw new NullPointerException("Player "+to+" could not be found.");
            player2.teleport(
                player1.getServerWorld(),
                player1.getX(),
                player1.getY(),
                player1.getZ(),
                Set.of(),
                player2.getPitch(),
                player2.getYaw(),
                false
            );
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To teleport player "+from+" to player "+to));
        }
    }
    
    @Override
    public void teleport(@NotNull UUID from, @Nullable String world, @Nullable Double x, @Nullable Double y, @Nullable Double z, @Nullable Float pitch, @Nullable Float yaw) {
        try {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(from);
            if(player == null) throw new NullPointerException("Player "+from+" could not be found.");
            
            player.teleport(
                world == null ? player.getServerWorld() : this.server.getWorld(this.server.getWorldRegistryKeys().stream().filter(w->w.getValue().getPath().equals(world)).findAny().orElse(null)),
                x == null ? player.getX() : x,
                y == null ? player.getY() : y,
                z == null ? player.getZ() : z,
                Set.of(),
                pitch == null ? player.getPitch() : pitch,
                yaw == null ? player.getYaw() : yaw,
                false
            );
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To teleport player "+from+" to "+x+", "+y+", "+z+", ("+pitch+", "+yaw+")"));
        }
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
    public <T> CommandManager<T> commandManager() {
        return (CommandManager<T>) this.commandManager;
    }
    
    @Override
    public void log(@NotNull Component message) {
        ((Audience) this.server).sendMessage(message);
    }
}
