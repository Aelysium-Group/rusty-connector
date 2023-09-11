package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import javax.websocket.*;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class WebSocketListener extends Endpoint {
    private CountDownLatch lock = new CountDownLatch(0);

    public void awaitClose() throws InterruptedException {
        this.lock.await();
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.lock = new CountDownLatch(1);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.lock.countDown();
    }
}