package group.aelysium.rustyconnector.core.plugin.central;

import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.core.plugin.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.core.plugin.lib.services.PacketBuilderService;
import group.aelysium.rustyconnector.core.plugin.lib.services.ServerInfoService;

import java.util.Map;

public class CoreServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public CoreServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public CoreServiceHandler() {
        super();
    }

    public RedisConnector messenger() {
        return this.find(RedisConnector.class).orElseThrow();
    }
    public MagicLinkService magicLink() {
        return this.find(MagicLinkService.class).orElseThrow();
    }
    public MessageCacheService messageCache() {
        return this.find(MessageCacheService.class).orElseThrow();
    }
    public PacketBuilderService packetBuilder() {
        return this.find(PacketBuilderService.class).orElseThrow();
    }
    public ServerInfoService serverInfo() {
        return this.find(ServerInfoService.class).orElseThrow();
    }
    public DynamicTeleportService dynamicTeleport() {
        return this.find(DynamicTeleportService.class).orElseThrow();
    }
}