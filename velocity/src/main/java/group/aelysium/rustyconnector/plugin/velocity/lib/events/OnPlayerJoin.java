package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import group.aelysium.rustyconnector.core.lib.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.firewall.WhitelistPlayer;
import net.kyori.adventure.text.Component;

import java.net.MalformedURLException;

public class OnPlayerJoin {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerJoin(PostLoginEvent event) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        Player player = event.getPlayer();

        return EventTask.async(() -> {
            try {
                // Check if there's a whitelist, run it if there is.
                Whitelist whitelist = plugin.getProxy().getProxyWhitelist();
                if(whitelist != null) {
                    String ip = player.getRemoteAddress().getHostString();

                    WhitelistPlayer whitelistPlayer = new WhitelistPlayer(player.getUsername(), player.getUniqueId(), ip);

                    if (!whitelist.validate(whitelistPlayer))
                        player.disconnect(Component.text(whitelist.getMessage()));
                }

                ServerFamily<? extends PaperServerLoadBalancer> rootFamily = plugin.getProxy().getRootFamily();

                rootFamily.connect(player);
            } catch (Exception e) {
                player.disconnect(Component.text("There was an error connecting to the network."));
                e.printStackTrace();
            }
        });
    }
}