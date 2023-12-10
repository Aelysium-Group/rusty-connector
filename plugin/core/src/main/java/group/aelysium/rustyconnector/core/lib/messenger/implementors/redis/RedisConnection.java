package group.aelysium.rustyconnector.core.lib.messenger.implementors.redis;

import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.message_cache.IMessageCacheService;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.model.FailService;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Vector;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisConnection extends MessengerConnection implements IMessengerConnection {
    private final Vector<RedisSubscriber> subscribers = new Vector<>();
    private final RedisPublisher publisher;
    private final RedisClient.Builder clientBuilder;
    private boolean isAlive = false;
    private ExecutorService executorService;
    private final FailService failService;
    private AESCryptor cryptor;

    public RedisConnection(PacketOrigin origin, RedisClient.Builder clientBuilder, AESCryptor cryptor) {
        super(origin);
        this.clientBuilder = clientBuilder;

        this.publisher = new RedisPublisher(this.clientBuilder.build(), cryptor);
        this.failService = new FailService(5, LiquidTimestamp.from(2, TimeUnit.SECONDS));
        this.cryptor = cryptor;
    }

    protected void subscribe(IMessageCacheService<?> cache, PluginLogger logger, InetSocketAddress originAddress) {
        if(!this.isAlive) return;

        this.executorService.submit(() -> {
            try {
                RedisSubscriber redis = new RedisSubscriber(this.cryptor, RedisConnection.this.clientBuilder.build(), cache, logger, this.origin, originAddress);
                RedisConnection.this.subscribers.add(redis);

                redis.subscribeToChannel(RedisConnection.this.failService);

                RedisConnection.this.subscribers.remove(redis);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    RedisConnection.this.failService.trigger("RedisService has failed to many times within the allowed amount of time! Please check the error messages and try again!");
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return;
                }
            }

            RedisConnection.this.subscribe(cache, logger, originAddress);
        });
    }

    public void startListening(IMessageCacheService<?> cache, PluginLogger logger, InetSocketAddress originAddress) {
        if(this.isAlive) throw new IllegalStateException("The RedisService is already running! You can't start it again! Shut it down with `.kill()` first and then try again!");
        this.executorService = Executors.newFixedThreadPool(3);

        this.isAlive = true;

        this.subscribe(cache, logger, originAddress);
    }

    @Override
    public void kill() {
        this.isAlive = false;
        this.failService.kill();

        for (Iterator<RedisSubscriber> iterator = this.subscribers.elements().asIterator(); iterator.hasNext(); ) {
            RedisSubscriber subscriber = iterator.next();
            subscriber.shutdown();
        }

        try {
            this.executorService.shutdown();
            try {
                if (!this.executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    this.executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.executorService.shutdownNow();
            }
        } catch (Exception ignore) {}

        try {
            this.publisher.shutdown();
        } catch (Exception ignore) {}
    }

    public <P extends IPacket> void publish(P packet) {
        this.publisher.publish((GenericPacket) packet);
    }

    @Override
    public void listen(PacketListener listener) {
        this.subscribers.forEach(subscribers -> subscribers.listen(listener));
    }
}
