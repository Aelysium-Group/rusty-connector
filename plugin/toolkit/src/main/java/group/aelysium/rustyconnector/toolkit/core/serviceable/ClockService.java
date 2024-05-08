package group.aelysium.rustyconnector.toolkit.core.serviceable;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClockService implements Service {
    protected final ScheduledExecutorService executorService;

    public ClockService(int threads) {
        this.executorService = Executors.newScheduledThreadPool(threads);
    }

    /**
     * Schedule a new task to run every `heartbeat`
     *
     * @param runnable The runnable task to run.
     * @param period   The intervals in seconds to wait before executing the runnable again.
     */
    public void scheduleRecurring(Runnable runnable, LiquidTimestamp period) {
        this.executorService.scheduleAtFixedRate(runnable, 0, period.value(), period.unit());
    }

    /**
     * Schedule a new task to run every `heartbeat`
     *
     * @param runnable The runnable task to run.
     * @param period   The intervals in seconds to wait before executing the runnable again.
     * @param delay    The intervals in seconds to wait before executing the runnable for the first time.
     */
    public void scheduleRecurring(Runnable runnable, long period, long delay) {
        this.executorService.scheduleAtFixedRate(runnable, delay, period, TimeUnit.SECONDS);
    }

    /**
     * Schedule a new task to run every `heartbeat`
     * @param runnable The runnable task to run.
     * @param period The intervals in {@link LiquidTimestamp#unit() LiquidTimestamp.unit()} to wait before executing the runnable again.
     * @param delay The intervals in {@link LiquidTimestamp#unit() LiquidTimestamp.unit()} to wait before executing the runnable for the first time.
     */
    public ScheduledFuture<?> scheduleRecurring(Runnable runnable, LiquidTimestamp period, LiquidTimestamp delay) {
        return this.executorService.scheduleAtFixedRate(runnable, delay.value(), period.value(), period.unit());
    }

    /**
     * Schedule a new task to run after `delay` amount of seconds.
     * @param runnable The runnable task to run.
     * @param delay The amount of time in seconds to wait before executing the runnable.
     */
    public void scheduleDelayed(Runnable runnable, LiquidTimestamp delay) {
        this.executorService.schedule(runnable, delay.value(), delay.unit());
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
