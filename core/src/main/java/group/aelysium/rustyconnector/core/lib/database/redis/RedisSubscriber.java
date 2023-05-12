package group.aelysium.rustyconnector.core.lib.database.redis;

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RedisSubscriber {
    private CountDownLatch lock = new CountDownLatch(0);
    private final RedisClient client;
    public RedisSubscriber(RedisClient client) {
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
            e.printStackTrace();
            this.lock.countDown();
        }
    }

    /**
     * Dispose of all Redis subscriptions and close all open connections.
     * This RedisIO becomes worthless after this is used.
     */
    public void shutdown() {
        this.lock.countDown();
        this.lock.countDown();
        this.lock.countDown();
        try {
            this.client.shutdown(2, 2, TimeUnit.SECONDS);
        } catch (Exception ignore) {}
    }

    /**
     * Called by `.subscribeToChannel()` when a message is received over the data channel.
     * @param rawMessage The message received.
     */
    protected void onMessage(String rawMessage) {
        // Empty adapter method
    }

    protected class RedisListener extends RedisPubSubAdapter<String, String> {
        @Override
        public void message(String channel, String message) {
            RedisSubscriber.this.onMessage(message);
        }
    }
}