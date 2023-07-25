package group.aelysium.rustyconnector.plugin.velocity.lib;

import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.util.Timer;
import java.util.TimerTask;

public class Clock {
    private final Timer timer = new Timer();
    private final long delay;
    private final Callable callback;

    public Clock(Callable callback) {
        this.callback = callback;
        this.delay = 10;
    }
    public Clock(Callable callback, long delay) {
        this.callback = callback;
        this.delay = delay;
    }

    public void start() {
        PluginLogger logger = VelocityAPI.get().logger();
        this.timer.schedule( new TimerTask() {
            public void run() {
                try {
                    Clock.this.callback.execute();
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
            }
        }, 0, this.delay*1000);
    }

    public void end() {
        this.timer.cancel();
        this.timer.purge();
    }
}
