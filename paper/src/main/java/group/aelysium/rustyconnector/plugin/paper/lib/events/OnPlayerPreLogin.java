package group.aelysium.rustyconnector.plugin.paper.lib.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.tpa.TPARequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerPreLogin implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if(event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_FULL) event.allow();
    }
}