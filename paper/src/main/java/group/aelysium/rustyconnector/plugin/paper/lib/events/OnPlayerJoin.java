package group.aelysium.rustyconnector.plugin.paper.lib.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.tpa.TPARequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        TPARequest tpaRequest = plugin.getVirtualServer().getTPAQueue().findClient(event.getPlayer().getPlayerProfile().getName());
        if(tpaRequest == null) return;
        try {
            tpaRequest.resolveClient();

            try {
                tpaRequest.teleport();
            } catch (NullPointerException e) {
                event.getPlayer().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(tpaRequest.getTarget().getPlayerProfile().getName()));
            }
        } catch (NullPointerException e) {
            event.getPlayer().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(tpaRequest.getTarget().getPlayerProfile().getName()));
        }

        plugin.getVirtualServer().getTPAQueue().removeAllPlayersRequests(event.getPlayer());
    }
}