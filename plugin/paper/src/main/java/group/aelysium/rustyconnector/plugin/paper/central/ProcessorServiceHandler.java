package group.aelysium.rustyconnector.plugin.paper.central;


import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.RedisMessagerService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ServerInfoService;

import java.util.Map;

public class ProcessorServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public ProcessorServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public ProcessorServiceHandler() {
        super();
    }

    public RedisService redisService() {
        return this.find(RedisService.class).orElseThrow();
    }
    public MagicLinkService magicLinkService() {
        return this.find(MagicLinkService.class).orElseThrow();
    }
    public MessageCacheService messageCacheService() {
        return this.find(MessageCacheService.class).orElseThrow();
    }
    public RedisMessagerService redisMessagerService() {
        return this.find(RedisMessagerService.class).orElseThrow();
    }
    public ServerInfoService serverInfoService() {
        return this.find(ServerInfoService.class).orElseThrow();
    }
    public DynamicTeleportService dynamicTeleportService() {
        return this.find(DynamicTeleportService.class).orElseThrow();
    }
}