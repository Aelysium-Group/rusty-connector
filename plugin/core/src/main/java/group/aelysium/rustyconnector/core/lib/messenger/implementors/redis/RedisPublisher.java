package group.aelysium.rustyconnector.core.lib.messenger.implementors.redis;

import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisConnectionStateAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.util.concurrent.TimeUnit;

public class RedisPublisher {
    private final RedisClient client;
    private StatefulRedisPubSubConnection<String, String> connection;
    private final AESCryptor cryptor;
    protected RedisPublisher(RedisClient client, AESCryptor cryptor) {
        this.client = client;
        this.client.addListener(new RedisPublisherListener());
        this.cryptor = cryptor;
    }

    /**
     * This RedisPublisher becomes worthless after this is used.
     */
    public void shutdown() {
        try {
            this.client.shutdownAsync();
        } catch (Exception ignore) {}
    }

    /**
     * Sends a message over a Redis data channel.
     * This method will also encrypt the packet using the Magic Link's AESCryptor
     * @param packet The message to send.
     * @throws IllegalStateException If you attempt to send a received RedisMessage.
     */
    public void publish(Packet packet) {
        String signedPacket;
        try {
            signedPacket = this.cryptor.encrypt(packet.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(this.connection == null) this.connection = this.client.connectPubSub();
        if(!this.connection.isOpen()) this.connection = this.client.connectPubSub();

        RedisPubSubAsyncCommands<String, String> async = connection.async();

        async.publish(this.client.dataChannel(), signedPacket);
    }

    static class RedisPublisherListener extends RedisConnectionStateAdapter {
        @Override
        public void onRedisExceptionCaught(RedisChannelHandler<?, ?> connection, Throwable cause) {
            cause.printStackTrace();
        }
    }
}
