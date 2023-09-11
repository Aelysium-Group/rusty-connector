package group.aelysium.rustyconnector.plugin.paper.lib;

import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
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

    public ConnectorsService connectors() {
        return this.find(ConnectorsService.class).orElseThrow();
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