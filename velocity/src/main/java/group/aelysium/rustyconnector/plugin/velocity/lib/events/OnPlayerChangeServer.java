package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.WhitelistPlayer;
import net.kyori.adventure.text.Component;

public class OnPlayerChangeServer {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerChangeServer(ServerConnectedEvent event) {
            return EventTask.async(() -> {
                try {
                    VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
                    PaperServer newServer = plugin.getProxy().findServer(event.getServer().getServerInfo());

                    if(newServer == null)
                        plugin.logger().log("The server that this player is joining doesn't seem to exist!");
                    else
                        newServer.playerJoined();

                    if(event.getPreviousServer().isPresent()) {
                        PaperServer oldServer = plugin.getProxy().findServer(event.getPreviousServer().get().getServerInfo());

                        if(oldServer == null)
                            plugin.logger().log("The server that this player is leaving doesn't seem to exist!");
                        else
                            oldServer.playerLeft();
                    }
                } catch (Exception e) {
                    VelocityRustyConnector.getInstance().logger().log(e.getMessage());
                }
            });
    }
}