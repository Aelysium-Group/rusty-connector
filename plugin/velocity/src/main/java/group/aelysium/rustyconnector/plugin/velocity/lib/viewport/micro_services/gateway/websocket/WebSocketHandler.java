package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.ViewportSession;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import javax.naming.AuthenticationException;

@WebSocket
public class WebSocketHandler {

    public WebSocketHandler() {}

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        ViewportService viewportService = Tinder.get().services().viewportService().orElseThrow();
        System.out.println("connection!");
        ViewportSession viewportSession = ViewportSession.with(session.getUpgradeRequest().getHeader("Authentication").toCharArray());

        if(viewportSession == null) {
            session.close(500, "Unable to authenticate.");
            return;
        }
        viewportService.services().gatewayService().websocket().join(viewportSession, null);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) throws AuthenticationException {
        System.out.println("disconnection!");
        ViewportSession viewportSession = ViewportSession.with(session.getUpgradeRequest().getHeader("Authentication").toCharArray());

        if(viewportSession == null) return;

        viewportSession.logout();
    }
}