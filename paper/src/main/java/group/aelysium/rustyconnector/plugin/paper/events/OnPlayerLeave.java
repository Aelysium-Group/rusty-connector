package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static group.aelysium.rustyconnector.plugin.paper.central.Processor.ValidServices.DYNAMIC_TELEPORT_SERVICE;

public class OnPlayerLeave implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        PaperAPI api = PaperRustyConnector.getAPI();

        api.getService(DYNAMIC_TELEPORT_SERVICE).orElseThrow().removeAllPlayersRequests(event.getPlayer());
    }
}