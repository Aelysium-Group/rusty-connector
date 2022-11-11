package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.PaperServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class onPlayerLeave implements Listener {
    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent event) {
        PaperServer server = PaperRustyConnector.getInstance().getVirtualServer();

        server.setPlayerCount(PaperRustyConnector.getInstance().getServer().getOnlinePlayers().size());
    }
}
