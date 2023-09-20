package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;

public class TPACleaningService extends ClockService {
    protected final LiquidTimestamp heartbeat;

    public TPACleaningService(LiquidTimestamp heartbeat) {
        super(3);
        this.heartbeat = heartbeat;
    }

    public void startHeartbeat() {
        Tinder api = Tinder.get();
        TPAService tpaService = api.services().dynamicTeleportService().orElseThrow()
                                   .services().tpaService().orElseThrow();
        this.scheduleRecurring(() -> {
            for(TPAHandler handler : tpaService.allTPAHandlers()) {
                try {
                    handler.clearExpired();
                } catch (Exception ignore) {}
            }
        }, this.heartbeat);
    }
}
