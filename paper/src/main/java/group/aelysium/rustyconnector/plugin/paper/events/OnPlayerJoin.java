package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models.CoordinateRequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PaperAPI api = PaperAPI.get();

        CoordinateRequest tpaRequest = api.services().dynamicTeleportService().findClient(event.getPlayer().getPlayerProfile().getName());
        if(tpaRequest == null) return;
        try {
            tpaRequest.resolveClient();

            try {
                tpaRequest.teleport();
            } catch (NullPointerException e) {
                event.getPlayer().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(tpaRequest.target().getPlayerProfile().getName()));
            }
        } catch (Exception e) {
            event.getPlayer().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(tpaRequest.target().getPlayerProfile().getName()));
        }

        api.services().dynamicTeleportService().removeAllPlayersRequests(event.getPlayer());
    }
}