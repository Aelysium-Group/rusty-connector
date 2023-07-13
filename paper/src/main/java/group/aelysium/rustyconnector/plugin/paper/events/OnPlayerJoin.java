package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models.DynamicTeleport_TPARequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static group.aelysium.rustyconnector.plugin.paper.central.Processor.ValidServices.DYNAMIC_TELEPORT_SERVICE;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PaperAPI api = PaperRustyConnector.getAPI();

        DynamicTeleport_TPARequest tpaRequest = api.getService(DYNAMIC_TELEPORT_SERVICE).orElseThrow().findClient(event.getPlayer().getPlayerProfile().getName());
        if(tpaRequest == null) return;
        try {
            tpaRequest.resolveClient();

            try {
                tpaRequest.teleport();
            } catch (NullPointerException e) {
                event.getPlayer().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(tpaRequest.getTarget().getPlayerProfile().getName()));
            }
        } catch (Exception e) {
            event.getPlayer().sendMessage(PaperLang.TPA_FAILED_TELEPORT.build(tpaRequest.getTarget().getPlayerProfile().getName()));
        }

        api.getService(DYNAMIC_TELEPORT_SERVICE).orElseThrow().removeAllPlayersRequests(event.getPlayer());
    }
}