package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from a paper server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        return EventTask.async(() -> {
            plugin.getProxy().findServer(event.getPlayer().getCurrentServer().orElseThrow().getServerInfo()).playerLeft();
        });
    }
}