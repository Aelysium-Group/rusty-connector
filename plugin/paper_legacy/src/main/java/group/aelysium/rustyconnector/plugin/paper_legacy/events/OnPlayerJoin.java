package group.aelysium.rustyconnector.plugin.paper_legacy.events;

import group.aelysium.rustyconnector.core.mcloader.lib.lang.MCLoaderLang;
import group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport.CoordinateRequest;
import group.aelysium.rustyconnector.plugin.paper_legacy.central.Tinder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Tinder api = Tinder.get();

        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        CoordinateRequest tpaRequest = api.services().dynamicTeleport().findClient(event.getPlayer().getName());
        if(tpaRequest == null) return;
        try {
            tpaRequest.resolveClient();

            try {
                tpaRequest.teleport();
            } catch (NullPointerException e) {
                event.getPlayer().sendMessage(
                        serializer.serialize(MCLoaderLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())))
                );
            }
        } catch (Exception e) {
            event.getPlayer().sendMessage(
                    serializer.serialize(MCLoaderLang.TPA_FAILED_TELEPORT.build(api.getPlayerName(tpaRequest.target())))
            );
        }

        api.services().dynamicTeleport().removeAllPlayersRequests(event.getPlayer().getUniqueId());
    }
}