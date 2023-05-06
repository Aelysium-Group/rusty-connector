package group.aelysium.rustyconnector.core.lib.database.redis;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisService {
    private final List<RedisSubscriber> liveRedisSubscribers = new ArrayList<>();
    private final RedisClient client;
    private boolean isAlive = false;
    ExecutorService executorService;

    public RedisService(RedisClient client) {
        this.client = client;
    }

    protected void launchNewRedisIO(Class<? extends RedisSubscriber> subscriber) {
        this.executorService.submit(() -> {

            RedisSubscriber redis = null;
            try {
                redis = subscriber.getDeclaredConstructor().newInstance(RedisService.this.client);
            } catch (Exception ignore) {}
            RedisService.this.liveRedisSubscribers.add(redis);

            try {
                redis.subscribeToChannel();
            } catch (Exception ignore) {}

            RedisService.this.liveRedisSubscribers.remove(redis);
            redis.shutdown();

            if(RedisService.this.isAlive)
                this.launchNewRedisIO(subscriber);
        });
    }

    /**
     * Start the service.
     * @param subscriber A class instance of the RedisSubscriber to be used by the service.
     * @throws IllegalStateException If the service is already running.
     */
    public void start(Class<? extends RedisSubscriber> subscriber) {
        if(this.isAlive) throw new IllegalStateException("The RedisService is already running! You can't start it again! Shut it down with `.kill()` first and then try again!");
        this.executorService = Executors.newFixedThreadPool(2);

        this.isAlive = true;

        this.launchNewRedisIO(subscriber);
    }

    /**
     * Kill the service.
     * This will disconnect all open RedisIOs and then shutdown any remaining threads.
     */
    public void kill() {
        this.isAlive = false;

        this.liveRedisSubscribers.forEach(RedisSubscriber::shutdown);

        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
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
