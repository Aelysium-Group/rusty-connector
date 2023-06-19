package group.aelysium.rustyconnector.core.lib.database.redis;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import io.lettuce.core.RedisChannelHandler;
import io.lettuce.core.RedisConnectionStateAdapter;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisService {
    private final Vector<RedisSubscriber> liveRedisSubscribers = new Vector<>();
    private RedisPublisher publisher;
    private char[] privateKey;
    private final RedisClient.Builder clientBuilder;
    private boolean isAlive = false;
    ExecutorService executorService;

    public RedisService(RedisClient.Builder clientBuilder, char[] privateKey) {
        this.clientBuilder = clientBuilder.setPrivateKey(privateKey);
        this.privateKey = privateKey;

        this.publisher = new RedisPublisher(this.clientBuilder.build());
    }

    protected void launchNewRedisSubscriber(Class<? extends RedisSubscriber> subscriber) {
        if(!this.isAlive) return;

        this.executorService.submit(() -> {
            try {
                RedisSubscriber redis = subscriber.getDeclaredConstructor(RedisClient.class).newInstance(RedisService.this.clientBuilder.build());
                RedisService.this.liveRedisSubscribers.add(redis);

                redis.subscribeToChannel();

                RedisService.this.liveRedisSubscribers.remove(redis);
            } catch (Exception e) {
                e.printStackTrace();
            }

            RedisService.this.launchNewRedisSubscriber(subscriber);
        });
    }

    /**
     * Start the service.
     * @param subscriber A class instance of the RedisSubscriber to be used by the service.
     * @throws IllegalStateException If the service is already running.
     */
    public void start(Class<? extends RedisSubscriber> subscriber) {
        if(this.isAlive) throw new IllegalStateException("The RedisService is already running! You can't start it again! Shut it down with `.kill()` first and then try again!");
        this.executorService = Executors.newFixedThreadPool(3);

        this.isAlive = true;

        this.launchNewRedisSubscriber(subscriber);
    }

    /**
     * Kill the service.
     * This will disconnect all open RedisIOs and then shutdown any remaining threads.
     */
    public void kill() {
        this.isAlive = false;

        for (Iterator<RedisSubscriber> it = this.liveRedisSubscribers.elements().asIterator(); it.hasNext(); ) {
            RedisSubscriber subscriber = it.next();
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

    public void publish(GenericRedisMessage message) {
        this.publisher.publish(message);
    }

    /**
     * Validate a private key.
     * @param privateKey The private key that needs to be validated.
     * @return `true` if the key is valid. `false` otherwise.
     */
    public boolean validatePrivateKey(char[] privateKey) {
        return Arrays.equals(this.privateKey, privateKey);
    }
}
