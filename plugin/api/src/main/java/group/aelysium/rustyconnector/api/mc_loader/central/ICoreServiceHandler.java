package group.aelysium.rustyconnector.api.mc_loader.central;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.api.mc_loader.dynamic_teleport.ICoordinateRequest;
import group.aelysium.rustyconnector.api.mc_loader.dynamic_teleport.IDynamicTeleportService;
import group.aelysium.rustyconnector.api.mc_loader.magic_link.IMagicLinkService;
import group.aelysium.rustyconnector.api.mc_loader.packet_builder.IPacketBuilderService;
import group.aelysium.rustyconnector.api.mc_loader.server_info.IServerInfoService;

public interface ICoreServiceHandler extends IServiceHandler {
    IMagicLinkService<? extends IPacketBuilderService> magicLink();
    IServerInfoService serverInfo();
    IPacketBuilderService packetBuilder();
    IDynamicTeleportService<? extends ICoordinateRequest> dynamicTeleport();
}