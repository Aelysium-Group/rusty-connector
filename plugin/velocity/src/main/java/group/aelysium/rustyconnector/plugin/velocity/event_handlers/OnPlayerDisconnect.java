package group.aelysium.rustyconnector.plugin.velocity.event_handlers;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from the proxy
     * This event prevents Velocity from attempting to connect the player to a velocity.toml server upon disconnect.
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {

        return EventTask.async(() -> {
            IPlayer player = new Player(event.getPlayer());

            RC.P.Adapter().onDisconnect(player);
        });
    }
}