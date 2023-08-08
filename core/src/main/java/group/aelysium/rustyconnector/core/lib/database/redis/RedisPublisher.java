package group.aelysium.rustyconnector.core.lib.database.redis;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisConnectionStateAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.util.concurrent.TimeUnit;

public class RedisPublisher {
    private final RedisClient client;
    private StatefulRedisPubSubConnection<String, String> connection;
    protected RedisPublisher(RedisClient client) {
        this.client = client;
        this.client.addListener(new RedisPublisherListener());
    }

    /**
     * This RedisPublisher becomes worthless after this is used.
     */
    public void shutdown() {
        try {
            this.client.shutdownAsync(2, 2, TimeUnit.SECONDS);
        } catch (Exception ignore) {}
    }

    /**
     * Sends a message over a Redis data channel.
     * If a message is not already, this method will sign messages with the private key provided via the RedisClient used to init this RedisPublisher.
     * @param message The message to send.
     * @throws IllegalStateException If you attempt to send a received RedisMessage.
     */
    public void publish(GenericRedisMessage message) {
        if(!message.sendable()) throw new IllegalStateException("Attempted to send a RedisMessage that isn't sendable!");

        try {
            message.signMessage(client.privateKey());
        } catch (IllegalStateException ignore) {} // If there's an issue it's because the message is already signed. Thus ready to send.

        if(this.connection == null) this.connection = this.client.connectPubSub();
        if(!this.connection.isOpen()) this.connection = this.client.connectPubSub();

        RedisPubSubAsyncCommands<String, String> async = connection.async();

        async.publish(this.client.dataChannel(), message.toString());
    }

    /**
     * Publish a message that will cause the subscriber to kill itself.
     * @deprecated This method should only ever be used for testing purposes.
     */
    @Deprecated
    public void publishKillable() {
        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            RedisPubSubAsyncCommands<String, String> async = connection.async();

            async.publish(this.client.dataChannel(), "DIE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class RedisPublisherListener extends RedisConnectionStateAdapter {
        @Override
        public void onRedisExceptionCaught(RedisChannelHandler<?, ?> connection, Throwable cause) {
            cause.printStackTrace();
        }
    }
}
