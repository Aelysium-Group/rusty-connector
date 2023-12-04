package group.aelysium.rustyconnector.core.mcloader.central;

import group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.RankedGameInterfaceService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.core.mcloader.lib.packet_builder.PacketBuilderService;
import group.aelysium.rustyconnector.core.mcloader.lib.server_info.ServerInfoService;

import java.util.Map;

public class CoreServiceHandler extends ServiceHandler implements ICoreServiceHandler {
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
    public RankedGameInterfaceService rankedGameInterface() {
        return this.find(RankedGameInterfaceService.class).orElseThrow();
    }
}