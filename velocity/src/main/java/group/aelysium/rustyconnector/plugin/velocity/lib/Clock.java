package group.aelysium.rustyconnector.plugin.velocity.lib;

import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.util.logger.LangKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

import java.util.Timer;
import java.util.TimerTask;

public class Clock {
    private final Timer timer = new Timer();
    private long delay = 10;

    public Clock(long delay) {
        this.delay = delay;
    }

    public void start(Callable callback) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        this.timer.schedule( new TimerTask() {
            public void run() {
                try {
                    callback.execute();
                } catch (Exception error) {
                    (new LangMessage(plugin.logger()))
                            .insert(error.getMessage())
                            .print();
                }
            }
        }, 0, this.delay*1000);
    }

    public void end() {
        this.timer.cancel();
    }
}
