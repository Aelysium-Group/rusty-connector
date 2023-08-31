package group.aelysium.rustyconnector.core.lib.database.redis;

import group.aelysium.rustyconnector.core.lib.model.FailService;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import ninja.leaping.configurate.reactive.TransactionFailedException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisService extends Service {
    private final Vector<RedisSubscriber> liveRedisSubscribers = new Vector<>();
    private final RedisPublisher publisher;
    private char[] privateKey;
    private final RedisClient.Builder clientBuilder;
    private boolean isAlive = false;
    private ExecutorService executorService;
    private final FailService failService;

    public RedisService(RedisClient.Builder clientBuilder, char[] privateKey) {
        this.clientBuilder = clientBuilder.setPrivateKey(privateKey);
        this.privateKey = privateKey;

        this.publisher = new RedisPublisher(this.clientBuilder.build());
        this.failService = new FailService(5, new LiquidTimestamp(2, TimeUnit.SECONDS));
    }

    protected void launchNewRedisSubscriber(Class<? extends RedisSubscriber> subscriber) {
        if(!this.isAlive) return;

        this.executorService.submit(() -> {
            try {
                RedisSubscriber redis = subscriber.getDeclaredConstructor(RedisClient.class).newInstance(RedisService.this.clientBuilder.build());
                RedisService.this.liveRedisSubscribers.add(redis);

                redis.subscribeToChannel(RedisService.this.failService);

                RedisService.this.liveRedisSubscribers.remove(redis);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    RedisService.this.failService.trigger("RedisService has failed to many times within the allowed amount of time! Please check the error messages and try again!");
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return;
                }
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
     * This will disconnect all open RedisSubscribers and then shutdown any remaining threads.
     */
    public void kill() {
        this.isAlive = false;
        this.failService.kill();

        for (Iterator<RedisSubscriber> iterator = this.liveRedisSubscribers.elements().asIterator(); iterator.hasNext(); ) {
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
