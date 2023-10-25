package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket;

import group.aelysium.rustyconnector.core.lib.crypt.MD5;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.viewport.events.ServerChatEvent;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;

public class WebSocketService implements Service {
    private final spark.Service spark;
    private final String listenerEndpoint;
    private final WebSocketGateway gateway = new WebSocketGateway();

    public WebSocketService(spark.Service spark) {
        this.spark = spark;

        this.listenerEndpoint = "/"+MD5.generateMD5();
        this.spark.webSocket(this.listenerEndpoint, new WebSocketHandler());
    }

    public String websocketEndpoint() {
        return this.listenerEndpoint;
    }

    public WebSocketGateway gateway() { return this.gateway; }

    /**
     * Fire an event to all listening sessions.
     * @param event The event to send.
     */
    public void fire(ViewportEvent event) {
        APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();

        for (APIService.Session session : api.sessions()) {
            Session client = session.websocketClient().orElse(null);
            if(client == null)
                continue;
            if(!session.subscriptions().contains(event.getClass()))
                continue;

            try {
                client.getRemote().sendString(event.toJsonPacket());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void kill() {
        try {
            this.spark.stop();
        } catch (Exception ignore) {}
    }

    public class Events {
        public static Mapping SERVER_CHAT_EVENT = new Mapping(ServerChatEvent.class, "SERVER_CHAT");

        public static List<Mapping> toList() {
            List<Mapping> list = new ArrayList<>();
            list.add(SERVER_CHAT_EVENT);

            return list;
        }

        public record Mapping (Class<? extends ViewportEvent> clazz, String name) {
            @Override
            public String toString() {
                return name;
            }
        }
    }
}
