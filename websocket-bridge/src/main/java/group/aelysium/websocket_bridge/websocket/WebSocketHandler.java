package group.aelysium.websocket_bridge.websocket;

import group.aelysium.websocket_bridge.WebSocketBridge;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class WebSocketHandler {
    @OnWebSocketConnect
    public void onConnect(Session session) {
        WebSocketService websocket = WebSocketBridge.instance().webSocketService();
        String authentication = session.getUpgradeRequest().getHeader("Authentication");

        if(!websocket.checkKey(authentication.toCharArray())) {
            session.close(401, "Invalid registration.");
            return;
        }

        try {
            String originString = session.getUpgradeRequest().getHeader("RC-Origin");
            WebSocketService.Type origin = WebSocketService.Type.valueOf(originString);

            websocket.register(session, origin);
        } catch (IllegalArgumentException ignore) {
            session.close(400, "Invalid origin provided.");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session) {
        WebSocketService websocket = WebSocketBridge.instance().webSocketService();

        websocket.unregister(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        WebSocketService websocket = WebSocketBridge.instance().webSocketService();

        websocket.forward(session, message);
    }
}