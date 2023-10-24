package group.aelysium.rustyconnector.api.mc_loader.central;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.ServiceHandler;

public interface ICoreServiceHandler extends ServiceHandler {
    RedisConnector messenger();
    MagicLinkService magicLink();
    MessageCacheService messageCache();
    PacketBuilderService packetBuilder();
    ServerInfoService serverInfo();
    DynamicTeleportService dynamicTeleport();
}