package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageTPAQueuePlayer;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;

public class TPACleaningService extends ClockService {
    protected final LiquidTimestamp heartbeat;

    public TPACleaningService(LiquidTimestamp heartbeat) {
        super(3);
        this.heartbeat = heartbeat;
    }

    public void startHeartbeat() {
        VelocityAPI api = VelocityAPI.get();
        TPAService tpaService = api.services().dynamicTeleportService().orElseThrow()
                                   .services().tpaService().orElseThrow();
        this.scheduleRecurring(() -> {
            for(TPAHandler handler : tpaService.getAllTPAHandlers()) {
                try {
                    handler.clearExpired();
                } catch (Exception ignore) {}
            }
        }, this.heartbeat);
    }
}
