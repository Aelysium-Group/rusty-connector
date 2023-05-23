package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.central.Processor;

public class LoadBalancingService extends ClockService {
    protected final long heartbeat;
    public LoadBalancingService(int threads, long heartbeat) {
        super(true, threads);
        this.heartbeat = heartbeat;
    }

    public void init() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        for (BaseServerFamily family : api.getService(FamilyService.class).dump()) {
            if (!(family instanceof PlayerFocusedServerFamily)) continue;

            this.scheduleRecurring(() -> {
                try {
                    PluginLogger logger = api.getLogger();

                    ((PlayerFocusedServerFamily) family).getLoadBalancer().completeSort();
                    if (logger.getGate().check(GateKey.FAMILY_BALANCING))
                        VelocityLang.FAMILY_BALANCING.send(logger, family);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, this.heartbeat);
        }
    }
}
