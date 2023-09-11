package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;

public abstract class WebSocketSubscriber extends MessengerSubscriber {
    protected WebSocketListener listener = new WebSocketListener();

    public WebSocketSubscriber(char[] privateKey, MessageCacheService cache, PluginLogger logger) {
        super(privateKey, cache, logger);
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