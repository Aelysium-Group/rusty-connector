package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket;

import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

@WebSocket
public class WebSocketHandler {

    public WebSocketHandler() {}

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
       WebSocketGateway gateway = Tinder.get().services().viewportService().orElseThrow().services().api().websocket().gateway();
       gateway.hang(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String rawMessage) throws Exception {
        APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();
        try {
            // Only sessions that are hanging are allowed to send a ticket request
            if(!api.websocket().gateway().isHung(session)) throw new AuthenticationException();

            WebSocketTicket message = WebSocketTicket.serialize(rawMessage);

            api.registerWebsocket(session, message.getAuth());
        } catch (AuthenticationException e) {
            e.printStackTrace();
            session.close(500, "Unable to authenticate!");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            session.close(500, "Not connected to websocket!");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) throws AuthenticationException {
        APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();
        WebSocketGateway gateway = api.websocket().gateway();
        gateway.unhang(session);

        try {
            api.logout(session);
        } catch (Exception e) {

        }
    }

    @OnWebSocketError
    public void onError(Throwable e) {
        e.printStackTrace();
    }
}