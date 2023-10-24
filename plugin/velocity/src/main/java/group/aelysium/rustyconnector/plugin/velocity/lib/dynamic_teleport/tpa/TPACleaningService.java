package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.api.core.serviceable.ClockService;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa.ITPACleaningService;
import group.aelysium.rustyconnector.api.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

public class TPACleaningService extends ClockService implements ITPACleaningService<TPAService> {
    protected final LiquidTimestamp heartbeat;

    public TPACleaningService(LiquidTimestamp heartbeat) {
        super(3);
        this.heartbeat = heartbeat;
    }

    public void startHeartbeat(TPAService tpaService) {
        this.scheduleRecurring(() -> {
            for(TPAHandler handler : tpaService.allTPAHandlers()) {
                try {
                    handler.clearExpired();
                } catch (Exception ignore) {}
            }
        }, this.heartbeat);
    }
}
