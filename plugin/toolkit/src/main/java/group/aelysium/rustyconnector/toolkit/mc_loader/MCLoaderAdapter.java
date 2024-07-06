package group.aelysium.rustyconnector.toolkit.mc_loader;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The MCLoader adapter exists to take loader specific actions and adapt them so that RustyConnector
 * can properly execute them regardless of disparate data types between the wrapper and RustyConnector.
 */
public abstract class MCLoaderAdapter {
    public abstract void setMaxPlayers(int max);

    public abstract int onlinePlayerCount();

    public abstract UUID playerUUID(@NotNull String username);

    public abstract String playerUsername(@NotNull UUID uuid);

    public abstract boolean isOnline(@NotNull UUID uuid);

    public abstract void teleportPlayer(@NotNull UUID player, @NotNull UUID target_player);

    public abstract void sendMessage(UUID uuid, Component component);
}
