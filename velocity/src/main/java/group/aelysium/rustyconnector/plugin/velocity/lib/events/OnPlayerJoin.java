package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import rustyconnector.generic.lib.generic.Lang;
import rustyconnector.generic.lib.generic.whitelist.Whitelist;
import rustyconnector.generic.lib.generic.server.Proxy;

public class OnPlayerJoin {
    @Subscribe
    public void onPlayerJoin(PostLoginEvent event, Proxy proxy) {
        Player player = event.getPlayer();

        Whitelist whitelist = proxy.getWhitelist();
        if(whitelist == null) return;

        String ip = player.getRemoteAddress().getHostString();
        if(!whitelist.validate(player.getUsername(), player.getUniqueId(), ip)) player.disconnect(Lang.get("When Player Isn't Whitelisted"));
        return;
    }
}