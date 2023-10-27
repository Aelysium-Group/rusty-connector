package group.aelysium.websocket_bridge.websocket;

import com.google.gson.JsonParseException;
import group.aelysium.websocket_bridge.WebSocketBridge;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.concurrent.TimeoutException;

@WebSocket
public class WebSocketHandler {
    @OnWebSocketConnect
    public void onConnect(Session session) {
        WebSocketService websocket = WebSocketBridge.instance().webSocketService();
        String authentication = session.getUpgradeRequest().getHeader("Authentication");

        try {
            websocket.checkAuthentication(authentication);
        } catch (JsonParseException | ArithmeticException ignore) {
            session.close(498, "Invalid Token.");
            return;
        } catch (TimeoutException ignore) {
            session.close(498, "Expired Token.");
            return;
        }

        try {
            String originString = session.getUpgradeRequest().getHeader("RC-Origin");
            WebSocketService.Type origin = WebSocketService.Type.valueOf(originString);

            websocket.register(session, origin);
        } catch (IllegalArgumentException ignore) {
            session.close(422, "Invalid origin provided.");
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