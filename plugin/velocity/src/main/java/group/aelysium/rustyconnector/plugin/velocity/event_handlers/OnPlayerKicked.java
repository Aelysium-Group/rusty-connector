package group.aelysium.rustyconnector.plugin.velocity.event_handlers;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.proxy.ProxyAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Objects;

public class OnPlayerKicked {
    /**
     * Runs when a player disconnects from a player server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerKicked(KickedFromServerEvent event) {
        return EventTask.async(() -> {
            Player player = new Player(event.getPlayer());
            String reason = null;
            if(event.getServerKickReason().isPresent())
                reason = PlainTextComponentSerializer.plainText().serialize(event.getServerKickReason().orElseThrow());

            ProxyAdapter.PlayerKickedResponse response = RC.P.Adapter().onKicked(player, reason);

            if(response.shouldDisconnect())
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(Objects.requireNonNullElse(response.reason(), "Kicked from server"))));
            else
                event.setResult(KickedFromServerEvent.RedirectPlayer.create((RegisteredServer) response.redirect().raw(), Component.text(Objects.requireNonNullElse(response.reason(), "Kicked from server"))));
        });
    }
}