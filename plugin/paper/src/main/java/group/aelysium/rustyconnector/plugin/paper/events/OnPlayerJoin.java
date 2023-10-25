package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;
import group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.CoordinateRequest;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Tinder api = Tinder.get();

        CoordinateRequest tpaRequest = api.services().dynamicTeleport().findClient(event.getPlayer().getPlayerProfile().getName());
        if(tpaRequest == null) return;
        try {
            tpaRequest.resolveClient();

            try {
                tpaRequest.teleport();
            } catch (NullPointerException e) {
                event.getPlayer().sendMessage(PluginLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())));
            }
        } catch (Exception e) {
            event.getPlayer().sendMessage(PluginLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())));
        }

        if (!((group.aelysium.rustyconnector.plugin.paper.central.Tinder) api).isFolia()) return;
        api.services().dynamicTeleport().removeAllPlayersRequests(event.getPlayer().getUniqueId());
    }
}