package group.aelysium.rustyconnector.core.lib.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClockService extends Service {
    protected final ScheduledExecutorService executorService;

    public ClockService(boolean enabled, int threads) {
        super(enabled);

        this.executorService = Executors.newScheduledThreadPool(threads);
    }

    /**
     * Schedule a new task to run every `heartbeat`
     * @param runnable The runnable task to run.
     * @param period The intervals in seconds to wait before executing the runnable again.
     */
    public ScheduledFuture<?> scheduleRecurring(Runnable runnable, long period) {
        return this.executorService.scheduleAtFixedRate(runnable, 0, period, TimeUnit.SECONDS);
    }

    /**
     * Schedule a new task to run every `heartbeat`
     * @param runnable The runnable task to run.
     * @param delay The amount of time in seconds to wait before executing the runnable.
     */
    public void scheduleDelayed(Runnable runnable, long delay) {
        this.executorService.schedule(runnable, delay, TimeUnit.SECONDS);
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
