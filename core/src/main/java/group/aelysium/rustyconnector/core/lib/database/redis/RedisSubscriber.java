package group.aelysium.rustyconnector.core.lib.database.redis;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RedisIO {
    private CountDownLatch lock = new CountDownLatch(0);
    private final RedisClient client;
    protected RedisIO(RedisClient client) {
        this.client = client;
    }

    /**
     * Subscribe to a specific Redis data channel.
     * This method is thread locking.
     */
    public void subscribeToChannel() {
        if(this.lock.getCount() != 0) throw new RuntimeException("Channel subscription is already active for this RedisIO! Either kill it with .shutdow(). Or create a new RedisIO to use!");

        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            this.lock = new CountDownLatch(1);

            RedisPubSubCommands<String, String> sync = connection.sync();

            connection.addListener(new RedisListener());

            sync.subscribe(this.client.getDataChannel());

            this.lock.await();
        } catch (Exception e) {
            this.lock.countDown();
        }
    }

    /**
     * Dispose of all Redis subscriptions and close all open connections.
     * This RedisIO becomes worthless after this is used.
     */
    public void shutdown() {
        this.lock.countDown();
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
     * @param message The message to send.
     */
    protected void publish(String message) {
        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            RedisPubSubAsyncCommands<String, String> async = connection.async();

            async.publish(this.client.getDataChannel(), message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected class RedisListener extends RedisPubSubAdapter<String, String> {
        @Override
        public void message(String channel, String message) {
            RedisIO.this.onMessage(message);
        }
    }
}