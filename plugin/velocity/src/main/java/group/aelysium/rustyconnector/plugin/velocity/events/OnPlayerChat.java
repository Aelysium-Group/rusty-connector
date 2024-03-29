package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.viewport.events.ServerChatEvent;

public class OnPlayerChat {
    /**
     * Runs when a player disconnects from a player server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerKicked(PlayerChatEvent event) {
        Tinder api = Tinder.get();

        return EventTask.async(() -> {
            if(api.services().viewportService().isEmpty()) return;

            try {
                api.services().viewportService().orElseThrow().services().api().websocket().fire(new ServerChatEvent(event.getPlayer(), event.getMessage()));
            } catch (Exception ignore) {}
        });
    }
}