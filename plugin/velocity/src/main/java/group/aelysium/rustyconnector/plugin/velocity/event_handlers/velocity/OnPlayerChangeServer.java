package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.*;

import java.util.UUID;

public class OnPlayerChangeServer {
    /**
     * Also runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerChangeServer(ServerConnectedEvent event) {
            return EventTask.async(() -> {
                Player player = new Player(event.getPlayer());

                try {
                    RegisteredServer newRawServer = event.getServer();
                    RegisteredServer oldRawServer = event.getPreviousServer().orElse(null);

                    MCLoader newServer = new MCLoader.Reference(UUID.fromString(newRawServer.getServerInfo().getName())).get();

                    if (oldRawServer == null) { // Player just connected to proxy. This isn't a server switch.
                        proxyJoin(newServer, player);
                        return;
                    }
                    MCLoader oldServer = new MCLoader.Reference(UUID.fromString(oldRawServer.getServerInfo().getName())).get();

                    serverSwitch(oldServer, newServer, player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    /**
     * Fires when a player first joins the proxy.
     */
    protected void proxyJoin(MCLoader newServer, Player player) {
        EventDispatch.UnSafe.fireAndForget(new FamilyPostJoinEvent(newServer.family(), newServer, player));
        EventDispatch.UnSafe.fireAndForget(new NetworkJoinEvent(newServer.family(), newServer, player));
    }

    /**
     * Fires when the player is switching from one server on the proxy to another server on the proxy.
     * Regardless of the families that poses those servers.
     */
    protected void serverSwitch(MCLoader oldServer, MCLoader newServer, Player player) {
        boolean isTheSameFamily = newServer.family().equals(oldServer.family());

        if(!isTheSameFamily) familySwitch(oldServer, newServer, player);

        EventDispatch.UnSafe.fireAndForget(new FamilyInternalSwitchEvent(newServer.family(), oldServer, newServer, player));
        EventDispatch.UnSafe.fireAndForget(new MCLoaderLeaveEvent(oldServer, player, false));
        EventDispatch.UnSafe.fireAndForget(new MCLoaderJoinEvent(newServer, player));
        EventDispatch.UnSafe.fireAndForget(new MCLoaderSwitchEvent(oldServer, newServer, player));
    }

    /**
     * Fires if the player is switching from one family to another family.
     */
    protected void familySwitch(MCLoader oldServer, MCLoader newServer, Player player) {
        EventDispatch.UnSafe.fireAndForget(new FamilySwitchEvent(oldServer.family(), newServer.family(), oldServer, newServer, player));
        EventDispatch.UnSafe.fireAndForget(new FamilyLeaveEvent(oldServer.family(), oldServer, player, false));
        EventDispatch.UnSafe.fireAndForget(new FamilyPostJoinEvent(newServer.family(), newServer, player));
    }
}