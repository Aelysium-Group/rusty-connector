package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Proxy;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.core.lib.generic.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.generic.firewall.WhitelistPlayer;

public class OnPlayerKickedFromSubServer {
    @Subscribe
    public void onPlayerJoin(KickedFromServerEvent event, Proxy proxy) {
        Player player = event.getPlayer();
        ServerConnection currentServer = player.getCurrentServer().orElse(null);
    }
}