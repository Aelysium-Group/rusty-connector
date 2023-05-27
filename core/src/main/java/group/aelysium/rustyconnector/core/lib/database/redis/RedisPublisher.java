package group.aelysium.rustyconnector.core.lib.database.redis;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

public class RedisPublisher {
    private final RedisClient client;
    protected RedisPublisher(RedisClient client) {
        this.client = client;
    }

    /**
     * Sends a message over a Redis data channel.
     * If a message is not already, this method will sign messages with the private key provided via the RedisClient used to init this RedisPublisher.
     * Kills the client after it's been used.
     * @param message The message to send.
     * @throws IllegalStateException If you attempt to send a received RedisMessage.
     */
    public void publish(GenericRedisMessage message) {
        if(!message.isSendable()) throw new IllegalStateException("Attempted to send a RedisMessage that isn't sendable!");

        try {
            message.signMessage(client.getPrivateKey());
        } catch (IllegalStateException ignore) {} // If there's an issue it's because the message is already signed. Thus ready to send.

        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            RedisPubSubAsyncCommands<String, String> async = connection.async();

            async.publish(this.client.getDataChannel(), message.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.client.shutdown();
    }

    /**
     * Publish a message that will cause the subscriber to kill itself.
     * Kills the client after it's been used.
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

        this.client.shutdown();
    }
}
