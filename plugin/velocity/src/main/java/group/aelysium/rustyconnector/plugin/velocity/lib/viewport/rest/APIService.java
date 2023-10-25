package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest;

import group.aelysium.rustyconnector.api.core.UserPass;
import group.aelysium.rustyconnector.core.lib.crypt.Token;
import group.aelysium.rustyconnector.api.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.viewport.endpoints.GetFamiliesEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.viewport.endpoints.GetFamilyEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.endpoints.LoginEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.endpoints.LogoutEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.endpoints.SubscribeChannelEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket.WebSocketService;
import spark.Route;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;
import java.util.*;

import static spark.Service.ignite;

public class APIService implements Service {
    private final Map<String, Session> sessions = new HashMap<>();
    private final WebSocketService websocket;
    private final spark.Service spark;
    private final Token tokenGenerator = new Token(32);
    private final APISettings settings;

    public APIService(InetSocketAddress address, APISettings settings) {
        this.settings = settings;

        this.spark = ignite();
        this.spark.ipAddress(address.getHostName()).port(address.getPort());

        this.websocket = new WebSocketService(this.spark);

        {
            Endpoints.toList().forEach(mapping -> {
                switch (mapping.type()) {
                    case GET -> this.spark.get(mapping.endpoint, mapping.handler);
                    case POST -> this.spark.post(mapping.endpoint, mapping.handler);
                    case DELETE -> this.spark.delete(mapping.endpoint, mapping.handler);
                }
            });

            enableCORS(this.spark);
        }
    }

    public List<Session> sessions() {
        return this.sessions.values().stream().toList();
    }

    /**
     * Generate a cryptographically secure 64 character alphanumeric string to be used as a user's session token.
     * @return A session token.
     */
    public String generateToken() {
        return this.tokenGenerator.nextString();
    }


    public void registerWebsocket(org.eclipse.jetty.websocket.api.Session websocketSession, String token) throws AuthenticationException {
        Session session = this.login(token, websocketSession.getRemoteAddress().getAddress().getHostAddress());
        session.websocketClient(websocketSession);

        this.websocket().gateway().unhang(websocketSession);
    }
    public Session login(String token, String ipAddress) throws AuthenticationException {
        Session session = this.sessions.get(token);

        if(session == null)
            throw new AuthenticationException("Unable to authenticate connection.");
        if(!session.ipAddress().equals(ipAddress))
            throw new AuthenticationException("Unable to authenticate connection.");

        return session;
    }

    public Session register(UserPass user, String ipAddress) throws AuthenticationException {
        if (!this.settings.credentials().user().equals(user.user()))
            throw new AuthenticationException("Unable to log in! The username was incorrect!");
        if (!Arrays.equals(this.settings.credentials().password(), user.password()))
            throw new AuthenticationException("Unable to log in! The password was incorrect!");

        Session session = Session.from(user, ipAddress);
        this.sessions.put(session.token(), session);
        return session;
    }

    public void logout(Session session) {
        this.sessions.remove(session.token());
        try {
            session.websocketClient().orElseThrow().close();
        } catch (Exception ignore) {}
    }
    public void logout(org.eclipse.jetty.websocket.api.Session session) {
        List<Session> foundSessions = this.sessions.values().stream().filter(item -> {
            try {
                return item.websocketClient().orElseThrow().equals(session);
            } catch (Exception ignore) {}

            return false;
        }).toList(); // Use a list in-case duplicates somehow exist

        foundSessions.forEach(foundSession -> {
            try {
                foundSession.websocketClient().orElseThrow().close();
            } catch (Exception ignore) {}
            this.sessions.remove(foundSession.token());
        });
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
            response.header("Access-Control-Request-Methods", "POST, GET, DELETE, OPTION");
            response.header("Access-Control-Allow-Headers", "*");
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }

    public WebSocketService websocket() {
        return this.websocket;
    }

    @Override
    public void kill() {
        this.sessions.forEach((token, session) -> {
            session.clearSubscriptions();
        });
        this.sessions.clear();
        this.websocket.kill();
        try {
            this.spark.stop();
        } catch (Exception ignore) {}
    }

    public class Endpoints {
        public static Endpoints.Mapping LOGIN = new Endpoints.Mapping("/login", HTTPRequestType.POST, new LoginEndpoint());
        public static Endpoints.Mapping LOGOUT = new Endpoints.Mapping("/logout", HTTPRequestType.GET, new LogoutEndpoint());
        public static Endpoints.Mapping SWITCH_WEBSOCKET_CHANNEL = new Endpoints.Mapping("/socket/:channel_id", HTTPRequestType.GET, new SubscribeChannelEndpoint());

        public static Endpoints.Mapping GET_FAMILIES = new Endpoints.Mapping("/family", HTTPRequestType.GET, new GetFamiliesEndpoint());
        public static Endpoints.Mapping GET_FAMILY = new Endpoints.Mapping("/family/:family_name", HTTPRequestType.GET, new GetFamilyEndpoint());
        public static List<Endpoints.Mapping> toList() {
            List<Endpoints.Mapping> list = new ArrayList<>();
            list.add(LOGIN);
            list.add(LOGOUT);
            list.add(SWITCH_WEBSOCKET_CHANNEL);
            list.add(GET_FAMILIES);
            list.add(GET_FAMILY);

            return list;
        }

        public record Mapping (String endpoint, HTTPRequestType type, Route handler) {
            @Override
            public String toString() {
                return type().name()+": "+endpoint;
            }
        }
    }

    public enum HTTPRequestType {
        GET,
        POST,
        DELETE
    }

    public record APISettings(LiquidTimestamp afkExpiration, UserPass credentials) {}

    public static class Session {
        private String token;
        private UserPass user;
        private String ipAddress;
        private Optional<org.eclipse.jetty.websocket.api.Session> websocketClient = Optional.empty();
        private Vector<WebSocketService.Events.Mapping> subscriptions = new Vector<>();

        private Session(String token, UserPass user, String ipAddress) {
            this.token = token;
            this.user = user;
            this.ipAddress = ipAddress;
        }

        public void subscribe(WebSocketService.Events.Mapping event) {
            if(this.subscriptions.contains(event)) return;
            this.subscriptions.add(event);
        }
        public void unsubscribe(WebSocketService.Events.Mapping event) {
            this.subscriptions.remove(event);
        }
        public void clearSubscriptions() {
            this.subscriptions.clear();
        }

        public List<WebSocketService.Events.Mapping> subscriptions() {
            return this.subscriptions;
        }

        public String token() {
            return this.token;
        }

        public UserPass user() {
            return this.user;
        }

        public String ipAddress() { return this.ipAddress; }

        public Optional<org.eclipse.jetty.websocket.api.Session> websocketClient() { return this.websocketClient; }
        public void websocketClient(org.eclipse.jetty.websocket.api.Session websocketClient) {
            if (this.websocketClient.isPresent())
                if(!this.websocketClient.get().equals(websocketClient))
                    this.websocketClient.orElseThrow().close();
            this.websocketClient = Optional.of(websocketClient);
        }

        public void logout() {
            this.subscriptions.clear();
            this.websocketClient().orElseThrow().close();
        }

        /**
         * Builds a {@link Session} from the provided {@link UserPass}
         * <br>
         * The session token can then be accessed using {@link Session#token()}.
         * @param user The {@link UserPass} to build from.
         * @return A {@link Session}.
         */
        protected static Session from(UserPass user, String ipAddress) {
            APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();

            return new Session(api.generateToken(), user, ipAddress);
        }
    }
}
