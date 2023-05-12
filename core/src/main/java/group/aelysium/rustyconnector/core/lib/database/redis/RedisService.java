package group.aelysium.rustyconnector.core.lib.database.redis;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisService {
    private final Vector<RedisSubscriber> liveRedisSubscribers = new Vector<>();
    private final RedisClient client;
    private boolean isAlive = false;
    ExecutorService executorService;

    public RedisService(RedisClient client) {
        this.client = client;
    }

    protected void launchNewRedisSubscriber(Class<? extends RedisSubscriber> subscriber) {
        this.executorService.submit(() -> {
            try {
                RedisSubscriber redis = subscriber.getDeclaredConstructor(RedisClient.class).newInstance(RedisService.this.client);
                RedisService.this.liveRedisSubscribers.add(redis);

                redis.subscribeToChannel();

                RedisService.this.liveRedisSubscribers.remove(redis);
                redis.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(RedisService.this.isAlive)
                this.launchNewRedisSubscriber(subscriber);
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

        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.executorService.shutdownNow();
        }
    }

    /**
     * Returns a new RedisPublisher which can be used to send messages to the data channel.
     * @return A RedisPublisher.
     */
    public RedisPublisher getMessagePublisher() {
        return new RedisPublisher(this.client);
    }

    /**
     * Validate a private key.
     * @param privateKey The private key that needs to be validated.
     * @return `true` if the key is valid. `false` otherwise.
     */
    public boolean validatePrivateKey(char[] privateKey) {
        return Arrays.equals(this.client.getPrivateKey(), privateKey);
    }
}
