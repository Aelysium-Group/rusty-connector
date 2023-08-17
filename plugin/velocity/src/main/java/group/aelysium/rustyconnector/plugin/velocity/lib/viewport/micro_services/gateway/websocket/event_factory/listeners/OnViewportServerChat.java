package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.event_factory.listeners;

import group.aelysium.rustyconnector.core.lib.event_factory.Listener;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.event_factory.events.ServerChatEvent;

public class OnViewportServerChat extends Listener<ServerChatEvent> {
    public OnViewportServerChat() {
        super(ServerChatEvent.class);
    }

    @Override
    public void execute(ServerChatEvent event) {

    }
}
