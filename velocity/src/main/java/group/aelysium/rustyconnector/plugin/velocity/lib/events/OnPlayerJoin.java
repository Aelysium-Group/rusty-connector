package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Proxy;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import rustyconnector.generic.lib.generic.Lang;
import rustyconnector.generic.lib.generic.whitelist.Whitelist;
import rustyconnector.generic.lib.generic.whitelist.WhitelistPlayer;

public class OnPlayerJoin {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe
    public void onPlayerJoin(PostLoginEvent event, Proxy proxy) {
        Player player = event.getPlayer();

        Whitelist whitelist = proxy.getProxyWhitelist();
        if(whitelist != null) {
            String ip = player.getRemoteAddress().getHostString();

            WhitelistPlayer whitelistPlayer = new WhitelistPlayer(player.getUsername(), player.getUniqueId(), ip);

            if (!whitelist.validate(whitelistPlayer))
                player.disconnect(Lang.getDynamic("When Player Isn't Whitelisted"));
        }

        ServerFamily rootFamily = proxy.getRootFamily();

        rootFamily.connect(player);
    }
}