package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.api.velocity.log_gate.GateKey;
import group.aelysium.rustyconnector.api.core.serviceable.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;

public class LoadBalancingService extends ClockService {
    protected final long heartbeat;
    public LoadBalancingService(int threads, long heartbeat) {
        super(threads);
        this.heartbeat = heartbeat;
    }

    public void init() {
        Tinder api = Tinder.get();
        for (BaseFamily family : api.services().family().dump()) {
            if (!(family instanceof PlayerFocusedServerFamily)) continue;

            this.scheduleRecurring(() -> {
                try {
                    PluginLogger logger = api.logger();

                    ((PlayerFocusedServerFamily) family).loadBalancer().completeSort();
                    if (logger.loggerGate().check(GateKey.FAMILY_BALANCING))
                        VelocityLang.FAMILY_BALANCING.send(logger, family);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, this.heartbeat);
        }
    }
}
