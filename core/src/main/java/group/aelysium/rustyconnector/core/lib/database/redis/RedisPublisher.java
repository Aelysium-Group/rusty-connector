package group.aelysium.rustyconnector.core.lib.database.redis;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisConnectionStateAdapter;
import io.lettuce.core.RedisConnectionStateListener;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.net.SocketAddress;

public class RedisPublisher {
    private final RedisClient client;
    private StatefulRedisPubSubConnection<String, String> connection;
    protected RedisPublisher(RedisClient client) {
        this.client = client;
        this.client.addListener(new RedisPublisherListener());
    }

    /**
     * Sends a message over a Redis data channel.
     * If a message is not already, this method will sign messages with the private key provided via the RedisClient used to init this RedisPublisher.
     * @param message The message to send.
     * @throws IllegalStateException If you attempt to send a received RedisMessage.
     */
    public void publish(GenericRedisMessage message) {
        System.out.println("making sure is sendable");
        if(!message.isSendable()) throw new IllegalStateException("Attempted to send a RedisMessage that isn't sendable!");

        try {
            System.out.println("Signing message...");
            message.signMessage(client.getPrivateKey());
        } catch (IllegalStateException ignore) {} // If there's an issue it's because the message is already signed. Thus ready to send.

        System.out.println("Connecting...");
        if(this.connection == null) this.connection = this.client.connectPubSub();
        if(!this.connection.isOpen()) this.connection = this.client.connectPubSub();

        RedisPubSubAsyncCommands<String, String> async = connection.async();

        System.out.println("Publishing...");
        async.publish(this.client.getDataChannel(), message.toString());

        System.out.println("Done.");
    }

    /**
     * Publish a message that will cause the subscriber to kill itself.
     * @deprecated This method should only ever be used for testing purposes.
     */
    @Deprecated
    public void publishKillable() {
        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            RedisPubSubAsyncCommands<String, String> async = connection.async();

            async.publish(this.client.getDataChannel(), "DIE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class RedisPublisherListener extends RedisConnectionStateAdapter {

        @Override
        public void onRedisConnected(RedisChannelHandler<?, ?> connection, SocketAddress socketAddress) {
            System.out.println("pub-Redis connected!");
        }
        @Override
        public void onRedisDisconnected(RedisChannelHandler<?, ?> connection) {
            System.out.println("pub-Redis closed!");
        }

        @Override
        public void onRedisExceptionCaught(RedisChannelHandler<?, ?> connection, Throwable cause) {
            cause.printStackTrace();
        }
    }
}
