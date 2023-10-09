package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import org.eclipse.jetty.websocket.api.Session;

import java.net.InetSocketAddress;
import java.util.*;

import static spark.Service.ignite;

public class WebSocketService extends Service {
    private Map<WebsocketChannel.Mapping, Vector<Session>> sessions = new HashMap<>();
    private spark.Service spark;

    public WebSocketService(InetSocketAddress address) {
        this.spark = ignite().ipAddress(address.getHostName()).port(address.getPort());

        this.sessions.put(WebsocketChannel.FAMILY_OVERVIEW, new Vector<>());
        this.spark.webSocket(WebsocketChannel.FAMILY_OVERVIEW.toString(), new WebSocketHandler(WebsocketChannel.FAMILY_OVERVIEW));

        this.sessions.put(WebsocketChannel.FAMILY_SPECIFIC, new Vector<>());
        this.spark.webSocket(WebsocketChannel.FAMILY_SPECIFIC.toString(), new WebSocketHandler(WebsocketChannel.FAMILY_SPECIFIC));

        this.sessions.put(WebsocketChannel.CHAT, new Vector<>());
        this.spark.webSocket(WebsocketChannel.CHAT.toString(), new WebSocketHandler(WebsocketChannel.CHAT));
        this.spark.init();
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

    public Vector<Session> sessions(WebsocketChannel.Mapping channel) {
        return this.sessions.get(channel);
    }

    public void publish(WebsocketChannel.Mapping channel, String message) {
        if(this.sessions.get(channel).size() == 0) return;

        this.sessions.get(channel).forEach(session -> {
            try {
                session.getRemote().sendString(message);
            } catch (Exception ignore) {}
        });
    }

    @Override
    public void kill() {
        this.spark.stop();
    }

    public class WebsocketChannel {
        public static Mapping FAMILY_OVERVIEW = new Mapping("/family", "FAMILY_OVERVIEW");

        /**
         * This mapping option must immediately be followed by the concatenated name of a family.
         */
        public static Mapping FAMILY_SPECIFIC = new Mapping("/family/", "FAMILY_SPECIFIC");
        public static Mapping CHAT = new Mapping("/chat", "CHAT");

        public static List<Mapping> toList() {
            List<Mapping> list = new ArrayList<>();
            list.add(FAMILY_OVERVIEW);
            list.add(FAMILY_SPECIFIC);
            list.add(CHAT);

            return list;
        }

        public record Mapping (String id, String name) {
            @Override
            public String toString() {
                return id;
            }
        }
    }
}
