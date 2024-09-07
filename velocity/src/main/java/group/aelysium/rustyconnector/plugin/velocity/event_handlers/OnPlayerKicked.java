package group.aelysium.rustyconnector.plugin.velocity.event_handlers;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;

public class OnPlayerKicked {
    /**
     * Runs when a player disconnects from a player server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerKicked(KickedFromServerEvent event) {
        return EventTask.async(() -> {
            Player player = RC.P.Adapter().convertToRCPlayer(event.getPlayer());
            RC.P.Adapter().onKicked(player, event.getServerKickReason().orElse(Component.text("Kicked by server.")));
        });
    }
}