package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;

public class WebSocketConnector extends MessengerConnector<WebSocketConnection> {
    protected final AESCryptor connectCryptor;

    private WebSocketConnector(AESCryptor packetCryptor, PacketOrigin origin, AESCryptor connectCryptor, InetSocketAddress address) {
        super(packetCryptor, origin, address, null);
        this.connectCryptor = connectCryptor;
    }

    @Override
    public WebSocketConnection connect() throws ConnectException {
        try {
            this.connection = new WebSocketConnection(
                    origin,
                    URI.create(this.address.getHostName()),
                    cryptor,
                    connectCryptor
            );
        } catch (IllegalArgumentException e) {
            throw new ConnectException(e.getMessage());
        }

        return this.connection;
    }

    /**
     * Creates a new {@link WebSocketConnector} and returns it.
     * @param packetCryptor The cryptor to use when logging into the websocket.
     * @param connectCryptor The cryptor to use when shipping messages.
     * @param address The {@link InetSocketAddress} that the connector points to.
     * @return A {@link WebSocketConnector}.
     */
    public static WebSocketConnector create(AESCryptor packetCryptor, PacketOrigin origin, AESCryptor connectCryptor, InetSocketAddress address) {
        return new WebSocketConnector(packetCryptor, origin, connectCryptor, address);
    }
}
