package group.aelysium.rustyconnector.core.lib.database;

import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.net.InetSocketAddress;
import java.util.Map;

public class RedisIO {
    private final RedisClient client;
    protected RedisIO(RedisClient client) {
        this.client = client;
    }

    /**
     * Subscribe to a specific Redis data channel.
     * @param channelName The name of the channel to subscribe to.
     */
    public void subscribeToChannel(String channelName) {
        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            RedisPubSubAsyncCommands<String, String> async = connection.async();

            connection.addListener(new RedisListener());

            async.subscribe(channelName);
        }
    }

    /**
     * Dispose of all Redis subscriptions and close all open connections.
     * This RedisIO becomes worthless after this is used.
     */
    public void shutdown() {
        this.client.shutdown();
    }

    /**
     * Called by `.subscribeToChannel()` when a message is received over the data channel.
     * @param rawMessage The message received.
     */
    protected void onMessage(String rawMessage) {
        // Empty adapter method
    }

    public void sendPluginMessage(String privateKey, RedisMessageType type, InetSocketAddress address, Map<String, String> parameters) throws IllegalArgumentException {
        // Empty adapter method
    }

    /**
     * Sends a message over a Redis data channel.
     * @param dataChannel The data channel to send over.
     * @param message The message to send.
     */
    protected void publish(String dataChannel, String message) {
        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            RedisPubSubAsyncCommands<String, String> async = connection.async();

            async.publish(dataChannel, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected class RedisListener extends RedisPubSubAdapter<String, String> {
        @Override
        public void message(String channel, String message) {
            System.out.println(message);
            RedisIO.this.onMessage(message);
        }
    }
}