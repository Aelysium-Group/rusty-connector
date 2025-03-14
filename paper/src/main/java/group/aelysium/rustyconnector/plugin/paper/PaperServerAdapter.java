package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.server.ServerAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PaperServerAdapter extends ServerAdapter {
    private final Server server;
    private final CommandManager<?> commandManager;

    public PaperServerAdapter(@NotNull Server server, @NotNull CommandManager<?> commandManager) {
        this.server = server;
        this.commandManager = commandManager;
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
    public Optional<String> playerID(@NotNull String username) {
        Player player = this.server.getPlayer(username);
        if(player == null) return Optional.empty();
        return Optional.of(player.getUniqueId().toString());
    }

    @Override
    public Optional<String> playerUsername(@NotNull String playerID) {
        Player player = this.server.getPlayer(UUID.fromString(playerID));
        if(player == null) return Optional.empty();
        return Optional.of(player.getName());
    }

    @Override
    public boolean isOnline(@NotNull String playerID) {
        Player player = this.server.getPlayer(UUID.fromString(playerID));
        if(player == null) return false;
        return player.isOnline();
    }
    
    @Override
    public void teleport(@NotNull String fromPlayer, @NotNull String toPlayer) {
        try {
            Player player1 = this.server.getPlayer(fromPlayer);
            if(player1 == null) throw new NullPointerException("Player "+fromPlayer+" could not be found.");
            Player player2 = this.server.getPlayer(toPlayer);
            if(player2 == null) throw new NullPointerException("Player "+toPlayer+" could not be found.");
            
            player2.teleportAsync(player1.getLocation());
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To teleport player "+fromPlayer+" to player "+toPlayer));
        }
    }
    
    @Override
    public void teleport(@NotNull UUID from, @Nullable String world, @Nullable Double x, @Nullable Double y, @Nullable Double z, @Nullable Float pitch, @Nullable Float yaw) {
        try {
            Player player = this.server.getPlayer(from);
            if(player == null) throw new NullPointerException("Player "+from+" could not be found.");
            
            player.teleportAsync(
                new Location(
                    world == null ? player.getWorld() : this.server.getWorld(world),
                    x == null ? player.getX() : x,
                    y == null ? player.getY() : y,
                    z == null ? player.getZ() : z,
                    pitch == null ? player.getPitch() : pitch,
                    yaw == null ? player.getYaw() : yaw
                )
            );
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To teleport player "+from+" to "+x+", "+y+", "+z+", ("+pitch+", "+yaw+")"));
        }
    }
    
    @Override
    public void messagePlayer(@NotNull String playerID, @NotNull Component message) {
        try {
            Player player = this.server.getPlayer(UUID.fromString(playerID));
            if(player == null) throw new NullPointerException("No player with the uuid "+playerID+" is online.");
            player.sendMessage((ComponentLike) message);
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
        this.server.getConsoleSender().sendMessage((ComponentLike) message);
    }
}
