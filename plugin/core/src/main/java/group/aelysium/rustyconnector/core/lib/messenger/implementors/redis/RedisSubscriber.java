package group.aelysium.rustyconnector.core.lib.messenger.implementors.redis;

import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.core.lib.model.FailService;
import group.aelysium.rustyconnector.toolkit.core.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.toolkit.core.message_cache.IMessageCacheService;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisConnectionStateAdapter;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RedisSubscriber extends MessengerSubscriber {
    private CountDownLatch lock = new CountDownLatch(0);
    private final RedisClient client;
    public RedisSubscriber(AESCryptor cryptor, RedisClient client, IMessageCacheService<? extends ICacheableMessage> cache, PluginLogger logger, Packet.Node senderUUID, Map<PacketIdentification, List<PacketListener<? extends Packet.Wrapper>>> listeners) {
        super(cryptor, cache, logger, senderUUID, listeners);
        this.client = client;
        this.client.addListener(new RedisSubscriberListener());
    }

    /**
     * Subscribe to a specific Redis data channel.
     * This method is thread locking.
     */
    public void subscribeToChannel(FailService failService) {
        if(this.lock.getCount() != 0) throw new RuntimeException("Channel subscription is already active for this RedisIO! Either kill it with .shutdow(). Or create a new RedisIO to use!");

        try (StatefulRedisPubSubConnection<String, String> connection = this.client.connectPubSub()) {
            this.lock = new CountDownLatch(1);

            RedisPubSubCommands<String, String> sync = connection.sync();

            connection.addListener(new RedisMessageListener());

            sync.subscribe(this.client.dataChannel());

            this.lock.await();
        } catch (Exception e) {
            e.printStackTrace();
            failService.trigger("RedisService has failed to many times within the allowed amount of time! Please check the error messages and try again!");
            this.lock.countDown();
        }
    }

    /**
     * Dispose of all Redis subscriptions and close all open connections.
     * This RedisSubscriber becomes worthless after this is used.
     */
    public void shutdown() {
        this.lock.countDown();
        this.lock.countDown();
        this.lock.countDown();

        try {
            this.client.shutdown();
        } catch (Exception ignore) {}
    }

    protected class RedisMessageListener extends RedisPubSubAdapter<String, String> {
        @Override
        public void message(String channel, String message) {
            RedisSubscriber.this.onMessage(message);
        }
    }

    static class RedisSubscriberListener extends RedisConnectionStateAdapter {
        @Override
        public void onRedisExceptionCaught(RedisChannelHandler<?, ?> connection, Throwable cause) {
            cause.printStackTrace();
        }
    }
}