package group.aelysium.rustyconnector.plugin.paper.lib.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerLeave implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        PaperRustyConnector.getInstance().getVirtualServer().getTPAQueue().removeAllPlayersRequests(event.getPlayer());
        PaperRustyConnector.getInstance().logger().log("Deleted player entries!");
    }
}