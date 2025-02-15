package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static net.kyori.adventure.text.format.NamedTextColor.BLUE;

public class OnPlayerChooseInitialServer {
    /**
     * Runs when a player first joins the proxy
     */
    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MIN_VALUE)
    public EventTask onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        return EventTask.async(() -> {
            try {
                Player player = RC.P.Adapter().convertToRCPlayer(event.getPlayer());
                Player.Connection.Request request = RC.P.Adapter().onInitialConnect(player, server -> {
                    RegisteredServer registeredServer = (RegisteredServer) server.metadata("velocity_RegisteredServer").orElse(null);
                    if(registeredServer == null) return Player.Connection.Request.failedRequest(player, "There are no servers available. If this issue persists please contact an admin.");

                    event.setInitialServer(registeredServer);
                    return Player.Connection.Request.successfulRequest(player, "Successfully set the initial server for the player!", server);
                });
                Player.Connection.Result result = request.result().get(10, TimeUnit.SECONDS);
                if(result.connected()) return;

                player.disconnect(result.message());
            } catch (Exception e) {
                RC.Error(Error.from(e).whileAttempting("To help a player `"+event.getPlayer().getUsername()+"` connect."));
                event.getPlayer().disconnect(Component.text("An internal error prevented you from connecting.", BLUE));
            }
        });
    }
}
