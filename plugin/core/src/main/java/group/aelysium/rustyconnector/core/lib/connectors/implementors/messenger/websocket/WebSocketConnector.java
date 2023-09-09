package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;

public class WebSocketConnector extends MessengerConnector<WebSocketConnection> {
    protected final char[] privateKey;
    protected final String connectKey;

    private WebSocketConnector(InetSocketAddress address, String connectKey, char[] privateKey) {
        super(address, null);
        this.privateKey = privateKey;
        this.connectKey = connectKey;
    }

    @Override
    public WebSocketConnection connect() throws ConnectException {
        this.connection = new WebSocketConnection(
            URI.create(this.address.getHostName()),
            this.privateKey
        );

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
    public static WebSocketConnector create(InetSocketAddress address, String connectKey, char @NotNull [] privateKey) {
        return new WebSocketConnector(address, connectKey, privateKey);
    }
}
