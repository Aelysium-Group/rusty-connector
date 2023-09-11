package group.aelysium.rustyconnector.plugin.paper.lib;

import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.PacketBuilderService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ServerInfoService;

import java.util.Map;

public class CoreServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public CoreServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public CoreServiceHandler() {
        super();
    }

    public RedisConnector redisService() {
        return this.find(RedisConnector.class).orElseThrow();
    }
    public MagicLinkService magicLinkService() {
        return this.find(MagicLinkService.class).orElseThrow();
    }
    public MessageCacheService messageCacheService() {
        return this.find(MessageCacheService.class).orElseThrow();
    }
    public PacketBuilderService redisMessagerService() {
        return this.find(PacketBuilderService.class).orElseThrow();
    }
    public ServerInfoService serverInfoService() {
        return this.find(ServerInfoService.class).orElseThrow();
    }
    public DynamicTeleportService dynamicTeleportService() {
        return this.find(DynamicTeleportService.class).orElseThrow();
    }
}