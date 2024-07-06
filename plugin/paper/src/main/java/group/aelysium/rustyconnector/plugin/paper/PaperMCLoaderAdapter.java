package group.aelysium.rustyconnector.plugin.paper;


import group.aelysium.rustyconnector.toolkit.mc_loader.MCLoaderAdapter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PaperMCLoaderAdapter extends MCLoaderAdapter {


    @Override
    public void setMaxPlayers(int max) {

    }

    @Override
    public int onlinePlayerCount() {
        return 0;
    }

    @Override
    public UUID playerUUID(@NotNull String username) {
        return null;
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
    public void teleportPlayer(@NotNull UUID player, @NotNull UUID target_player) {

    }

    @Override
    public void sendMessage(UUID uuid, Component component) {

    }
}
