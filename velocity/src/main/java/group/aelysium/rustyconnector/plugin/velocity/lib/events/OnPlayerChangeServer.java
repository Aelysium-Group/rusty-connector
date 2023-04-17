package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.VirtualProxyProcessor;

public class OnPlayerChangeServer {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerChangeServer(ServerConnectedEvent event) {
            return EventTask.async(() -> {
                VelocityAPI api = VelocityRustyConnector.getAPI();
                PluginLogger logger = api.getLogger();
                VirtualProxyProcessor virtualProcessor = api.getVirtualProcessor();

                try {
                    PlayerServer newServer = virtualProcessor.findServer(event.getServer().getServerInfo());

                    if(newServer == null)
                        logger.log("The server that this player is joining doesn't seem to exist!");
                    else
                        newServer.playerJoined();

                    if(event.getPreviousServer().isPresent()) {
                        PlayerServer oldServer = virtualProcessor.findServer(event.getPreviousServer().get().getServerInfo());

                        if(oldServer == null)
                            logger.log("The server that this player is leaving doesn't seem to exist!");
                        else
                            oldServer.playerLeft();
                    }
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
            });
    }
}