package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.event_factory.ViewportEvent;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.event_factory.events.ServerChatEvent;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.ViewportSession;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;

public class WebSocketService extends Service {
    private Map<ViewportSession, Class<? extends ViewportEvent>> sessions = Collections.synchronizedMap(new WeakHashMap<>());
    private spark.Service spark;

    public WebSocketService(spark.Service spark) {
        this.spark = spark;

        this.spark.webSocket("", new WebSocketHandler());
    }

    private static void enableCORS(spark.Service service) {
        service.options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        service.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Methods", "POST, GET");
            response.header("Access-Control-Allow-Headers", "*");
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }

    public Set<ViewportSession> sessions() {
        return this.sessions.keySet();
    }

    public void join(ViewportSession session, Class<ViewportEvent> eventClass) {
        this.sessions.put(session, eventClass);
    }

    public void leave(ViewportSession session) {
        this.sessions.remove(session);
    }

    public void updateListening(ViewportSession session, Class<? extends ViewportEvent> eventClass) {
        this.sessions.put(session, eventClass);
    }

    public List<ViewportSession> findListening(Class<? extends ViewportEvent> eventClass) {
        List<Map.Entry<ViewportSession, Class<? extends ViewportEvent>>> matchingSessionMappings = this.sessions.entrySet().stream().filter(entry -> entry.getValue() == eventClass).toList();

        List<ViewportSession> listeningSessions = new ArrayList<>();
        for (Map.Entry<ViewportSession, Class<? extends ViewportEvent>> sessionMapping : matchingSessionMappings) {
            listeningSessions.add(sessionMapping.getKey());
        }

        return listeningSessions;
    }

    /**
     * Fire an event to all listening sessions.
     * @param event The event to send.
     */
    public void fire(ViewportEvent event) {
        List<Map.Entry<ViewportSession, Class<? extends ViewportEvent>>> matchingSessionMappings = this.sessions.entrySet().stream().filter(entry -> entry.getValue() == event.getClass()).toList();

        for (Map.Entry<ViewportSession, Class<? extends ViewportEvent>> sessionMapping : matchingSessionMappings) {
            Session session = sessionMapping.getKey().websocketClient().orElse(null);
            if(session == null) {
                this.sessions.remove(sessionMapping.getKey());
                continue;
            }

            try {
                session.getRemote().sendString(event.toJsonPacket());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void kill() {
        this.sessions.forEach((key, value) -> {
            try {
                key.websocketClient().orElseThrow().close();
            } catch (Exception ignore) {}
        });
        this.sessions.clear();
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
