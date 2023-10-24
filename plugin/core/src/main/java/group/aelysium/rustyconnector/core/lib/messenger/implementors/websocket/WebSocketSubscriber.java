package group.aelysium.rustyconnector.core.lib.messenger.implementors.websocket;

import group.aelysium.rustyconnector.api.velocity.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;

import java.net.InetSocketAddress;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

public class WebSocketSubscriber extends MessengerSubscriber implements WebSocket.Listener {
    private CountDownLatch lock = new CountDownLatch(0);

    public WebSocketSubscriber(AESCryptor cryptor, MessageCacheService messageCache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers, PacketOrigin origin, InetSocketAddress originAddress) {
        super(cryptor, messageCache, logger, handlers, origin, originAddress);
    }

    public void awaitClose() throws InterruptedException {
        this.lock.await();
    }
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        this.onMessage(data.toString());
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        this.lock.countDown();
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }


    @Override
    public void onOpen(WebSocket webSocket) {
        this.lock = new CountDownLatch(1);
        WebSocket.Listener.super.onOpen(webSocket);
    }
}