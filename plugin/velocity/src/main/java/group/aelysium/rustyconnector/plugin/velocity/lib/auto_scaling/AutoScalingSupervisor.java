package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling;

import group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks.AutoScaleDispatch;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;

import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class AutoScalingSupervisor extends ClockService {
    protected ForkJoinPool pool = ForkJoinPool.commonPool();
    protected Optional<AutoScaleDispatch> task = Optional.empty();

    public AutoScalingSupervisor() {
        super(2);
    }

    public void start() {
        this.executorService.schedule(()->{
            if(task.isPresent())
                try {
                    pool.invoke(task.get());
                } catch (Exception ignore) {}

            this.start();
        }, 20, TimeUnit.SECONDS);
    }

    @Override
    public void kill() {
        pool.shutdownNow();
        super.kill();
    }
}
