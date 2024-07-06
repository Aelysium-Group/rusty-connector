package group.aelysium.rustyconnector.plugin.velocity.event_handlers;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;

import java.util.UUID;

public class OnPlayerChangeServer {
    /**
     * Also runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerChangeServer(ServerPreConnectEvent event) {
            return EventTask.async(() -> {
                Player player = new Player(event.getPlayer());
                RegisteredServer newRawServer = event.getOriginalServer();
                RegisteredServer oldRawServer = event.getPreviousServer();

                IMCLoader newServer = RC.P.MCLoader(UUID.fromString(newRawServer.getServerInfo().getName()));

                IMCLoader oldServer = null;
                try {
                    assert oldRawServer != null;
                    oldServer = RC.P.MCLoader(UUID.fromString(oldRawServer.getServerInfo().getName()));
                } catch (Exception ignore) {}

                RC.P.Adapter().onMCLoaderSwitch(player, oldServer, newServer);
            });
    }
}