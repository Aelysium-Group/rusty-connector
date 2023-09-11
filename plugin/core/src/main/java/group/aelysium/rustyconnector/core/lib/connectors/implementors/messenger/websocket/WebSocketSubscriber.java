package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerSubscriber;

public abstract class WebSocketSubscriber extends MessengerSubscriber {
    protected WebSocketListener listener = new WebSocketListener();

    public WebSocketSubscriber(char[] privateKey) {
        super(privateKey);
    }

    public MessageHandler handler() {
        return new MessageHandler();
    }
    public WebSocketListener listener() { return this.listener; }

    public class MessageHandler implements javax.websocket.MessageHandler {
        public void onMessage(String message) {
            WebSocketSubscriber.this.onMessage(message);
        }
    }
}