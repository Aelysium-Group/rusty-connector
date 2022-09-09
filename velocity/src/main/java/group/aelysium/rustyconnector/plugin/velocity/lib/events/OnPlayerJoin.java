package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Proxy;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.core.lib.generic.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.generic.firewall.WhitelistPlayer;
import net.kyori.adventure.text.Component;

import java.net.MalformedURLException;

public class OnPlayerJoin {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.NORMAL)
    public EventTask onPlayerJoin(PostLoginEvent event) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        return EventTask.async(() -> {
            Player player = event.getPlayer();

            Whitelist whitelist = plugin.getProxy().getProxyWhitelist();
            if(whitelist != null) {
                String ip = player.getRemoteAddress().getHostString();

                WhitelistPlayer whitelistPlayer = new WhitelistPlayer(player.getUsername(), player.getUniqueId(), ip);

                if (!whitelist.validate(whitelistPlayer))
                    player.disconnect(Component.text("You aren't whitelisted on this server!"));
            }

            ServerFamily rootFamily = plugin.getProxy().getRootFamily();

            try {
                rootFamily.connect(player);
            } catch (MalformedURLException e) {
                player.disconnect(Component.text("Unable to connect you to the network! There are no default servers available!"));
                plugin.logger().log("There are no servers registered in the root family! Player's will be unable to join your network if there are no servers here!");
            }
        });
    }
}