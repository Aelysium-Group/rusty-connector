package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerLeave implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Tinder api = Plugin.getAPI();

        api.services().dynamicTeleport().removeAllPlayersRequests(event.getPlayer().getUniqueId());
    }
}