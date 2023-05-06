package group.aelysium.rustyconnector.plugin.velocity.lib.processor;

import group.aelysium.rustyconnector.core.lib.Callable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadBalancingService {
    private final ScheduledExecutorService executorService;
    private final long heartbeat;

    public LoadBalancingService(int threads, long heartbeat) {
        this.executorService = Executors.newScheduledThreadPool(threads);
        this.heartbeat = heartbeat;
    }

    /**
     * Schedule a new task to run every `heartbeat`
     * @param runnable The runnable task to run.
     */
    public void schedule(Runnable runnable) {
        this.executorService.scheduleAtFixedRate(runnable, 0, this.heartbeat, TimeUnit.SECONDS);
    }
    
    public void kill() {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
        }
    }
}
