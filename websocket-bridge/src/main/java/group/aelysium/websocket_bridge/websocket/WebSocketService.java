package group.aelysium.websocket_bridge.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.net.InetSocketAddress;
import java.util.*;

import static spark.Service.ignite;

public class WebSocketService {
    private char[] key;
    private Vector<Session> serverSessions = new Vector<>();
    private Vector<Session> proxySessions = new Vector<>();
    private spark.Service spark;

    public WebSocketService(InetSocketAddress address, char[] key, boolean cors) {
        super();
        this.spark = ignite().ipAddress(address.getHostName()).port(address.getPort());

        this.spark.webSocket("/", WebSocketHandler.class);
        this.spark.init();

        this.key = key;

        if(cors) this.enableCORS();
    }

    private void enableCORS() {
        this.spark.options("/*", (request, response) -> {

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

        this.spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Methods", "POST, GET");
            response.header("Access-Control-Allow-Headers", "*");
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }

    /**
     * Forwards a message from the current session to all other sessions of the opposite type.
     * @param from The {@link Session} that this message originated from.
     * @param message The message.
     */
    public void forward(Session from, String message) {
        if(this.proxySessions.contains(from))
            this.serverSessions.forEach(session -> {
                try {
                    session.getRemote().sendString(message);
                } catch (Exception ignore) {}
            });

        if(this.serverSessions.contains(from))
            this.proxySessions.forEach(session -> {
                try {
                    session.getRemote().sendString(message);
                } catch (Exception ignore) {}
            });
    }

    public void register(Session session, Type type) {
        if(type == Type.PROXY && !this.proxySessions.contains(session)) this.proxySessions.add(session);
        if(type == Type.SERVER && !this.serverSessions.contains(session)) this.serverSessions.add(session);
    }

    public void unregister(Session session) {
        this.proxySessions.remove(session);
        this.serverSessions.remove(session);
    }

    public boolean checkKey(char[] key) {
        return Arrays.equals(this.key, key);
    }

    public void kill() {
        this.proxySessions.clear();
        this.serverSessions.clear();
        this.spark.stop();
    }

    public enum Type {
        PROXY,
        SERVER
    }
}
