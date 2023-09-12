package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.model.FailService;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisConnection extends MessengerConnection {
    private final Vector<RedisSubscriber> subscribers = new Vector<>();
    private final RedisPublisher publisher;
    private final char[] privateKey;
    private final RedisClient.Builder clientBuilder;
    private boolean isAlive = false;
    private ExecutorService executorService;
    private final FailService failService;

    public RedisConnection(RedisClient.Builder clientBuilder, char[] privateKey) {
        this.clientBuilder = clientBuilder.setPrivateKey(privateKey);
        this.privateKey = privateKey;

        this.publisher = new RedisPublisher(this.clientBuilder.build());
        this.failService = new FailService(5, new LiquidTimestamp(2, TimeUnit.SECONDS));
    }

    @Override
    protected void subscribe(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers) {
        if(!this.isAlive) return;

        this.executorService.submit(() -> {
            try {
                RedisSubscriber redis = new RedisSubscriber(RedisConnection.this.clientBuilder.build(), cache, logger, handlers);
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

            RedisConnection.this.subscribe(cache, logger, handlers);
        });
    }

    @Override
    public void startListening(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers) {
        if(this.isAlive) throw new IllegalStateException("The RedisService is already running! You can't start it again! Shut it down with `.kill()` first and then try again!");
        this.executorService = Executors.newFixedThreadPool(3);

        this.isAlive = true;

        this.subscribe(cache, logger, handlers);
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

    @Override
    public void publish(GenericPacket message) {
        this.publisher.publish(message);
    }

    @Override
    public boolean validatePrivateKey(char[] privateKey) {
        return Arrays.equals(this.privateKey, privateKey);
    }
}
