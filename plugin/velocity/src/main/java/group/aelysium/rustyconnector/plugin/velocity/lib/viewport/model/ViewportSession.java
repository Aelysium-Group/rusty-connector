package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model;

import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.GatewayService;
import org.eclipse.jetty.websocket.api.Session;

import javax.naming.AuthenticationException;
import java.util.Optional;

public class ViewportSession {
    private char[] token;
    private SyncedUser user;
    private Optional<Session> websocketClient = Optional.empty();

    private ViewportSession(char[] token, SyncedUser user) {
        this.token = token;
        this.user = user;
    }

    public char[] token() {
        return this.token;
    }

    public SyncedUser user() {
        return this.user;
    }

    public Optional<Session> websocketClient() {
        return this.websocketClient;
    }

    public void logout() {
        ViewportService viewportService = VelocityAPI.get().services().viewportService().orElseThrow();
        viewportService.services().gatewayService().websocket().leave(this);
    }

    /**
     * Searches the gateway service for a {@link ViewportSession} matching the token.
     * @param token The token to search for.
     * @return A {@link ViewportSession}.
     * @throws AuthenticationException If no {@link ViewportSession} matching the token could be found.
     */
    public static ViewportSession with(char[] token) throws AuthenticationException {
        GatewayService gatewayService = VelocityAPI.get().services().viewportService().orElseThrow().services().gatewayService();
        return gatewayService.resolveConnection(token);
    }

    /**
     * Builds a {@link ViewportSession} from the provided {@link SyncedUser}
     * <br>
     * The session token can then be accessed using {@link ViewportSession#token()}.
     * @param user The {@link SyncedUser} to build from.
     * @return A {@link ViewportSession}.
     */
    public static ViewportSession from(SyncedUser user) {
        GatewayService gatewayService = VelocityAPI.get().services().viewportService().orElseThrow().services().gatewayService();
        return new ViewportSession(gatewayService.generateToken(), user);
    }
}
