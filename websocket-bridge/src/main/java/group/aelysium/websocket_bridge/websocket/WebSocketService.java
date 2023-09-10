package group.aelysium.websocket_bridge.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import group.aelysium.websocket_bridge.AESCryptor;
import group.aelysium.websocket_bridge.LiquidTimestamp;
import org.eclipse.jetty.websocket.api.Session;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static spark.Service.ignite;

public class WebSocketService {
    private final SecureConnectorSettings secureConnectorSettings;
    private final Vector<Session> serverSessions = new Vector<>();
    private final Vector<Session> proxySessions = new Vector<>();
    private final spark.Service spark;
    private Optional<AESCryptor> cryptor = Optional.empty();

    public WebSocketService(InetSocketAddress address, boolean cors, SecureConnectorSettings secureConnectorSettings) {
        super();
        this.spark = ignite().ipAddress(address.getHostName()).port(address.getPort());

        this.spark.webSocket("/", WebSocketHandler.class);
        this.spark.init();

        this.secureConnectorSettings = secureConnectorSettings;

        if(this.secureConnectorSettings.enabled) this.cryptor = Optional.of(AESCryptor.create(Arrays.toString(this.secureConnectorSettings.key())));

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

    /**
     * Checks the authentication of the passed payload.
     * If no exceptions are thrown by this method, the payload can be considered properly authenticated.
     * @param authentication The payload to authenticate.
     * @throws JsonParseException If the payload can't be parsed into JSON.
     * @throws ArithmeticException If the payload doesn't have a valid connection key.
     * @throws TimeoutException If the payload is expired.
     */
    public void checkAuthentication(String authentication) throws JsonParseException, ArithmeticException, TimeoutException {
        if(this.cryptor.isEmpty()) return;

        AESCryptor cryptor = this.cryptor.orElseThrow();

        String decrypted;
        try {
            decrypted = cryptor.decrypt(authentication);
        } catch (Exception e) {
            throw new ArithmeticException();
        }

        JsonObject payload = JsonParser.parseString(decrypted).getAsJsonObject();

        long time = payload.getAsJsonPrimitive("time").getAsLong();
        if(time < this.secureConnectorSettings.timeout.epochBeforeNow()) throw new TimeoutException();
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

    public record SecureConnectorSettings(boolean enabled, char[] key, LiquidTimestamp timeout) {}
}
