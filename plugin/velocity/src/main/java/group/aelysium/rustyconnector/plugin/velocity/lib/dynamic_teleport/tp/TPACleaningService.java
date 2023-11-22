package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tp;

import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;

public class TPACleaningService extends ClockService {
    protected final LiquidTimestamp heartbeat;

    public TPACleaningService(LiquidTimestamp heartbeat) {
        super(3);
        this.heartbeat = heartbeat;
    }

    public void startHeartbeat(TPService tpService) {
        this.scheduleRecurring(() -> {
            for(TPAHandler handler : tpService.allTPAHandlers()) {
                try {
                    handler.clearExpired();
                } catch (Exception ignore) {}
            }
        }, this.heartbeat);
    }
}
