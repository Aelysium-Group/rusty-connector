package group.aelysium.rustyconnector.plugin.velocity.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.WebSocketService;

public class OnPlayerChat {
    /**
     * Runs when a player disconnects from a player server
     */
    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPlayerKicked(PlayerChatEvent event) {
        VelocityAPI api = VelocityAPI.get();

        return EventTask.async(() -> {
            if(api.services().viewportService().isEmpty()) return;

            try {
                api.services().viewportService().orElseThrow().services().gatewayService().websocket().publish(WebSocketService.WebsocketChannel.CHAT, event.getMessage());
            } catch (Exception ignore) {}
        });
    }
}