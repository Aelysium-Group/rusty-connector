package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class OnPlayerKicked {
    /**
     * Runs when a player disconnects from a player server
     */
    @Subscribe(priority = Short.MAX_VALUE)
    public EventTask onPlayerKicked(KickedFromServerEvent event) {
        return EventTask.async(() -> {
            Player player = RC.P.Adapter().convertToRCPlayer(event.getPlayer());
            RC.P.Adapter().onKicked(player, event.getServerKickReason().orElse(Component.text("Kicked by server.", NamedTextColor.RED)));
        });
    }
}