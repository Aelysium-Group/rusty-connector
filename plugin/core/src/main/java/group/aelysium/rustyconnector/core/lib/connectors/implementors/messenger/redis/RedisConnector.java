package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis;

import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.UserPass;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.net.InetSocketAddress;

public class RedisConnector extends MessengerConnector<RedisConnection> {
    private static final ClientResources resources = ClientResources.create();
    protected final String dataChannel;
    protected final ProtocolVersion protocolVersion;

    private RedisConnector(AESCryptor cryptor, PacketOrigin origin, InetSocketAddress address, UserPass userPass, ProtocolVersion protocolVersion, String dataChannel) {
        super(cryptor, origin, address, userPass);
        this.protocolVersion = protocolVersion;
        this.dataChannel = dataChannel;
    }

    @Override
    public RedisConnection connect() throws ConnectException {
        this.connection = new RedisConnection(
                origin,
            this.toClientBuilder(),
            this.cryptor
        );

        return this.connection;
    }

    private RedisClient.Builder toClientBuilder() {
        return new RedisClient.Builder()
                .setHost(this.address.getHostName())
                .setPort(this.address.getPort())
                .setUser(this.userPass.user())
                .setPassword(this.userPass.password())
                .setDataChannel(this.dataChannel)
                .setResources(resources)
                .setProtocol(this.protocolVersion);
    }

    /**
     * Creates a new {@link RedisConnector} and returns it.
     * The created {@link RedisConnector} is also automatically added to the {@link ConnectorsService}.
     * @param cryptor The cryptor to use when shipping messages.
     * @param address The {@link InetSocketAddress} that the connector points to.
     * @param userPass The {@link UserPass} to be used when authenticating with the remote resource.
     * @param protocolVersion The Redis protocol to use.
     * @param dataChannel The data channel to ship messages over.
     * @return A {@link RedisConnector}.
     */
    public static RedisConnector create(AESCryptor cryptor, PacketOrigin origin, InetSocketAddress address, UserPass userPass, ProtocolVersion protocolVersion, @NotNull String dataChannel) {
        return new RedisConnector(cryptor, origin, address, userPass, protocolVersion, dataChannel);
    }
}
