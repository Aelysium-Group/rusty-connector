package group.aelysium.rustyconnector.plugin.paper.events;

import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
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