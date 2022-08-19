package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Lang;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.server.Proxy;

public class OnPlayerJoin {
    @Subscribe
    public void onPlayerJoin(PostLoginEvent event, Proxy proxy) {
        Player player = event.getPlayer();

        Whitelist whitelist = proxy.getWhitelist();
        if(whitelist == null) return;

        if(!whitelist.validate(player)) player.disconnect(Lang.get("When Player Isn't Whitelisted"));
        return;
    }
}