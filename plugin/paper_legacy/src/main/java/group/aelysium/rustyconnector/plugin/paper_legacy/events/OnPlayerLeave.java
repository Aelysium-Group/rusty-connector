package group.aelysium.rustyconnector.plugin.paper_legacy.events;

import group.aelysium.rustyconnector.plugin.paper_legacy.central.Tinder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerLeave implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Tinder api = Tinder.get();

        api.services().dynamicTeleport().removeAllPlayersRequests(event.getPlayer().getUniqueId());
    }
}