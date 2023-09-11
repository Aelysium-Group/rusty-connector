package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class WebSocketHandler {
    private final WebSocketService.WebsocketChannel.Mapping channel;

    public WebSocketHandler(WebSocketService.WebsocketChannel.Mapping channel) {
        this.channel = channel;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        Tinder.get()
                .services().viewportService().orElseThrow()
                .services().gatewayService().websocket().sessions(this.channel).add(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Tinder.get()
                .services().viewportService().orElseThrow()
                .services().gatewayService().websocket().sessions(this.channel).remove(session);
    }
}