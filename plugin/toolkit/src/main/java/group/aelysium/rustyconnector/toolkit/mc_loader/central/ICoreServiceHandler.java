package group.aelysium.rustyconnector.toolkit.mc_loader.central;

import group.aelysium.rustyconnector.toolkit.core.events.EventManager;
import group.aelysium.rustyconnector.toolkit.core.packet.MCLoaderPacketBuilder;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.dynamic_teleport.ICoordinateRequest;
import group.aelysium.rustyconnector.toolkit.mc_loader.dynamic_teleport.IDynamicTeleportService;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.IMagicLinkService;
import group.aelysium.rustyconnector.toolkit.mc_loader.ranked_game_interface.IRankedGameInterfaceService;
import group.aelysium.rustyconnector.toolkit.mc_loader.server_info.IServerInfoService;

import java.util.Optional;

public interface ICoreServiceHandler extends IServiceHandler {
    /**
     * Gets the {@link EventManager event manager} which allows access to event based logic.
     * @return {@link EventManager}
     */
    EventManager events();
    IMagicLinkService magicLink();
    IServerInfoService serverInfo();
    Optional<? extends IRankedGameInterfaceService> rankedGameInterface();
    IDynamicTeleportService<? extends ICoordinateRequest> dynamicTeleport();
    MCLoaderPacketBuilder packetBuilder();
}