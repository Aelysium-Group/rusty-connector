package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis;

import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.UserPass;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.net.InetSocketAddress;

public class RedisConnector extends MessengerConnector<RedisConnection> {
    private static final ClientResources resources = ClientResources.create();
    protected final String dataChannel;
    protected final char[] privateKey;
    protected final ProtocolVersion protocolVersion;

    private RedisConnector(InetSocketAddress address, UserPass userPass, ProtocolVersion protocolVersion, String dataChannel, char[] privateKey) {
        super(address, userPass);
        this.protocolVersion = protocolVersion;
        this.dataChannel = dataChannel;
        this.privateKey = privateKey;
    }

    @Override
    public RedisConnection connect() throws ConnectException {
        this.connection = new RedisConnection(
            this.toClientBuilder(),
            this.privateKey
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
                .setPrivateKey(this.privateKey)
                .setResources(resources)
                .setProtocol(this.protocolVersion);
    }

    /**
     * Creates a new {@link RedisConnector} and returns it.
     * The created {@link RedisConnector} is also automatically added to the {@link ConnectorsService}.
     * @param address The {@link InetSocketAddress} that the connector points to.
     * @param userPass The {@link UserPass} to be used when authenticating with the remote resource.
     * @param protocolVersion The Redis protocol to use.
     * @param dataChannel The data channel to ship messages over.
     * @param privateKey The private key to use when shipping messages.
     * @return A {@link RedisConnector}.
     */
    public static RedisConnector create(InetSocketAddress address, UserPass userPass, ProtocolVersion protocolVersion, @NotNull String dataChannel, char @NotNull [] privateKey) {
        return new RedisConnector(address, userPass, protocolVersion, dataChannel, privateKey);
    }
}
