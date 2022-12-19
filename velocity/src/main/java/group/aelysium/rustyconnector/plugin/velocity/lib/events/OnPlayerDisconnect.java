package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from a paper server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {
        try {

            VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

            return EventTask.async(() -> {
                try {
                    ServerInfo info = event.getPlayer().getCurrentServer().orElseThrow().getServerInfo();
                    PaperServer server = plugin.getProxy().findServer(info);
                    if(server == null) return;
                    server.playerLeft();
                } catch (Exception ignore) {}
            });
        } catch (Exception ignore) {}
        return EventTask.async(() -> {});
    }
}