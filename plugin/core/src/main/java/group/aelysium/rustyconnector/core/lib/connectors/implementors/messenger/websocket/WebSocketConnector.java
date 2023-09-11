package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;

public class WebSocketConnector extends MessengerConnector<WebSocketConnection> {
    protected final char[] connectKey;
    protected final char[] privateKey;

    private WebSocketConnector(InetSocketAddress address, char[] connectKey, char[] privateKey) {
        super(address, null);
        this.connectKey = connectKey;
        this.privateKey = privateKey;
    }

    @Override
    public WebSocketConnection connect() throws ConnectException {
        try {
            this.connection = new WebSocketConnection(
                    URI.create(this.address.getHostName()),
                    this.connectKey,
                    this.privateKey
            );
        } catch (IllegalArgumentException e) {
            throw new ConnectException(e.getMessage());
        }

        return this.connection;
    }

    /**
     * Creates a new {@link WebSocketConnector} and returns it.
     * The created {@link WebSocketConnector} is also automatically added to the {@link ConnectorsService}.
     * @param address The {@link InetSocketAddress} that the connector points to.
     * @param connectKey The key to use when logging into the websocket.
     * @param privateKey The private key to use when shipping messages.
     * @return A {@link WebSocketConnector}.
     */
    public static WebSocketConnector create(InetSocketAddress address, char[] connectKey, char[] privateKey) {
        return new WebSocketConnector(address, connectKey, privateKey);
    }
}
