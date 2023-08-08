package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerLeave implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        PaperAPI api = PaperAPI.get();

        api.services().dynamicTeleportService().removeAllPlayersRequests(event.getPlayer());
    }
}