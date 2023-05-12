package group.aelysium.rustyconnector.core.lib.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClockService {
    protected final ScheduledExecutorService executorService;
    protected final long heartbeat;

    public ClockService(int threads, long heartbeat) {
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
