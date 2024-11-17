package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.proxy.player.Player;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from the proxy
     * This event prevents Velocity from attempting to connect the player to a velocity.toml server upon disconnect.
     */
    @Subscribe(priority = Short.MAX_VALUE)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {

        return EventTask.async(() -> {
            Player player = RC.P.Adapter().convertToRCPlayer(event.getPlayer());
            RC.P.Adapter().onDisconnect(player);
        });
    }
}