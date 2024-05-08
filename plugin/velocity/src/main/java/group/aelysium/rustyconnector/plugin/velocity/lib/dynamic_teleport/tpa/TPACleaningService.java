package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPACleaningService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAService;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

public class TPACleaningService extends ClockService implements ITPACleaningService {
    protected final LiquidTimestamp heartbeat;

    public TPACleaningService(LiquidTimestamp heartbeat) {
        super(3);
        this.heartbeat = heartbeat;
    }

    public void startHeartbeat(ITPAService tpaService) {
        this.scheduleRecurring(() -> {
            for(ITPAHandler handler : tpaService.allTPAHandlers()) {
                try {
                    handler.clearExpired();
                } catch (Exception ignore) {}
            }
        }, this.heartbeat);
    }
}
