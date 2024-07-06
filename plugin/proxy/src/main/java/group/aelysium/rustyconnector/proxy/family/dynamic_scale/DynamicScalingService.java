package group.aelysium.rustyconnector.proxy.family.dynamic_scale;

import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.proxy.family.Families;
import group.aelysium.rustyconnector.toolkit.common.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.proxy.events.family.RebalanceEvent;
import group.aelysium.rustyconnector.toolkit.proxy.util.DependencyInjector;
import group.aelysium.rustyconnector.toolkit.proxy.util.LiquidTimestamp;

public class DynamicScalingService extends ClockService {
    protected final LiquidTimestamp heartbeat;
    public DynamicScalingService(int threads, LiquidTimestamp heartbeat) {
        super(threads);
        this.heartbeat = heartbeat;
    }

    public void init(DependencyInjector.DI3<Families, PluginLogger, EventManager> deps) {
    }
}
