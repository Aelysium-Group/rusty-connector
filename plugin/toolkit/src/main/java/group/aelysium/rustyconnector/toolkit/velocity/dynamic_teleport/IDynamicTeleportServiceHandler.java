package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.hub.IHubService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPACleaningService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPARequest;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAService;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;

import java.util.Optional;

public interface IDynamicTeleportServiceHandler extends IServiceHandler {
    <TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer, TPlayerFocusedFamilyBase extends IPlayerFocusedFamilyBase<TPlayerServer, TResolvablePlayer>>
        Optional<IAnchorService<TPlayerServer, TResolvablePlayer, TPlayerFocusedFamilyBase>> anchorService();
    Optional<IHubService> hubService();
    <TTPACleaningService extends ITPACleaningService<?>, TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer, TPlayerFocusedFamilyBase extends IPlayerFocusedFamilyBase<TPlayerServer, TResolvablePlayer>, TTPARequest extends ITPARequest, TTPAHandler extends ITPAHandler<TTPARequest>>
        Optional<ITPAService<TTPACleaningService, TPlayerServer, TResolvablePlayer, TPlayerFocusedFamilyBase, TTPARequest, TTPAHandler>> tpaService();
}