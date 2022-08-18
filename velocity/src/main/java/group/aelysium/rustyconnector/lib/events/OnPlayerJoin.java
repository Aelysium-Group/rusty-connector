package group.aelysium.rustyconnector.lib.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.lib.generic.Lang;
import group.aelysium.rustyconnector.lib.generic.server.Proxy;
import group.aelysium.rustyconnector.lib.generic.Whitelist;

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