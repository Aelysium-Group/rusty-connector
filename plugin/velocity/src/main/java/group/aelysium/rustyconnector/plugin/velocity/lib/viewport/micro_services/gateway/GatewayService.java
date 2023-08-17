package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import group.aelysium.rustyconnector.core.lib.hash.Token;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.APIService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.WebSocketService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.ViewportSession;

import javax.naming.AuthenticationException;
import java.net.InetSocketAddress;

import static spark.Service.ignite;

public class GatewayService extends Service {
    private final Cache<char[], ViewportSession> sessions;
    private final spark.Service publicSpark;
    private final APIService api;
    private final Token tokenGenerator = new Token(32);

    public GatewayService(InetSocketAddress publicAddress, InetSocketAddress apiAddress, GatewaySettings settings) {
        this.sessions = CacheBuilder.newBuilder()
                .maximumSize(50)
                .expireAfterAccess(settings.afkExpiration().value(), settings.afkExpiration().unit())
                .build();

        {
            this.publicSpark = ignite();
            this.publicSpark.staticFiles.location("/viewport");
            this.publicSpark.ipAddress(publicAddress.getHostName()).port(publicAddress.getPort());

            this.publicSpark.init();
        }
        {
            this.api = new APIService(apiAddress);
        }
    }

    /**
     * Generate a cryptographically secure 64 character alphanumeric string to be used as a user's session token.
     * @return A session token.
     */
    public char[] generateToken() {
        return this.tokenGenerator.nextString().toCharArray();
    }

    public APIService api() {
        return this.api;
    }

    public WebSocketService websocket() {
        return this.api.websocket();
    }

    public void login(ViewportSession session) {
        this.sessions.put(session.token(), session);
    }
    public void logout(ViewportSession session) {
        this.sessions.invalidate(session.token());
    }

    public ViewportSession resolveConnection(char[] token) throws AuthenticationException {
        ViewportSession session = this.sessions.getIfPresent(token);
        if(session == null) throw new AuthenticationException("Unable to authenticate connection.");
        return session;
    }

    @Override
    public void kill() {
        this.sessions.invalidateAll();
        this.api.kill();
        this.publicSpark.stop();
    }

    public record GatewaySettings(LiquidTimestamp afkExpiration) {}
}
