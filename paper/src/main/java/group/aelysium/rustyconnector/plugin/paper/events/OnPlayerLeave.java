package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerLeave implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        PaperAPI api = PaperRustyConnector.getAPI();

        api.getService(DynamicTeleportService.class).removeAllPlayersRequests(event.getPlayer());
    }
}