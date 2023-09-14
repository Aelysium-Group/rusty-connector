package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;

import java.util.Map;

public class WebSocketSubscriber extends MessengerSubscriber {
    protected WebSocketListener listener = new WebSocketListener();

    public WebSocketSubscriber(AESCryptor cryptor, MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers, PacketOrigin origin) {
        super(cryptor, cache, logger, handlers, origin);
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