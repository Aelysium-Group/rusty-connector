package group.aelysium.rustyconnector.plugin.paper.lib.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerLeave implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        PaperAPI api = PaperRustyConnector.getAPI();

        api.getVirtualProcessor().getTPAQueue().removeAllPlayersRequests(event.getPlayer());
    }
}