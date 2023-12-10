package group.aelysium.rustyconnector.core.lib.messenger.implementors.redis;

import group.aelysium.rustyconnector.toolkit.core.UserPass;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Optional;

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
     * @param cryptor The cryptor to use when shipping messages.
     * @param spec The spec to load the connector with.
     * @return A {@link RedisConnector}.
     */
    public static RedisConnector create(AESCryptor cryptor, RedisConnectorSpec spec) {
        return new RedisConnector(cryptor, spec.origin(), spec.address(), spec.userPass(), spec.protocolVersion(), spec.dataChannel());
    }

    public record RedisConnectorSpec(PacketOrigin origin, InetSocketAddress address, UserPass userPass, ProtocolVersion protocolVersion, String dataChannel) { }


    @Override
    public void kill() {
        if(this.connection != null) this.connection.kill();
    }
}
