package group.aelysium.rustyconnector.plugin.velocity.event_handlers;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.proxy.players.Player;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.proxy.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.proxy.connection.IPlayerConnectable;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

public class OnPlayerChooseInitialServer {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        return EventTask.async(() -> {
            Player player = new Player(event.getPlayer());

            IPlayerConnectable.Request request = RC.P.Adapter().onInitialConnect(player);
            ConnectionResult result;
            try {
                result = request.result().get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                event.getPlayer().disconnect(Component.text("Connection timed out."));
                return;
            }

            if(!result.connected()) {
                event.getPlayer().disconnect(result.message());
                return;
            }

            event.setInitialServer((RegisteredServer) result.server().orElseThrow().raw());
        });
    }
}
