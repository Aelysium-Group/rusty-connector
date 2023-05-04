package group.aelysium.rustyconnector.core.lib.database;

import java.util.concurrent.CountDownLatch;

public class RedisSubscriptionRunnable implements Runnable {
    private final RedisIO redis;
    private final String dataChannel;

    public RedisSubscriptionRunnable(RedisIO redis, String dataChannel) {
        super();

        this.redis = redis;
        this.dataChannel = dataChannel;
    }

    @Override
    public void run() {
        try {
            final CountDownLatch lock = new CountDownLatch(1);
            this.redis.subscribeToChannel(this.dataChannel);
            lock.await();
        } catch (Exception e) {
            redis.shutdown();
        }
    }
}
