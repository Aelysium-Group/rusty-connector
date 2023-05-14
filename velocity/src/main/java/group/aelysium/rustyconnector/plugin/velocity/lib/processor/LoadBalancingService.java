package group.aelysium.rustyconnector.plugin.velocity.lib.processor;

import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;

public class LoadBalancingService extends ClockService {
    public LoadBalancingService(int threads, long heartbeat) {
        super(threads, heartbeat);
    }

    public void init() {
        VirtualProxyProcessor processor = VelocityRustyConnector.getAPI().getVirtualProcessor();
        for (BaseServerFamily family : processor.getFamilyManager().dump())
            this.schedule(() -> {
                try {
                    VelocityAPI api = VelocityRustyConnector.getAPI();
                    PluginLogger logger = api.getLogger();

                    family.getLoadBalancer().completeSort();
                    if(logger.getGate().check(GateKey.FAMILY_BALANCING))
                        VelocityLang.FAMILY_BALANCING.send(logger, family);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
}
