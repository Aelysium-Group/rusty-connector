package group.aelysium.rustyconnector.api.velocity.dynamic_teleport;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.hub.IHubService;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa.ITPACleaningService;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa.ITPAHandler;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa.ITPARequest;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa.ITPAService;
import group.aelysium.rustyconnector.api.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;

import java.util.Optional;

public interface IDynamicTeleportServiceHandler extends IServiceHandler {
    <TPlayerServer extends IPlayerServer, TPlayerFocusedFamilyBase extends IPlayerFocusedFamilyBase<TPlayerServer>>
        Optional<IAnchorService<TPlayerServer, TPlayerFocusedFamilyBase>> anchorService();
    Optional<IHubService> hubService();
    <TTPACleaningService extends ITPACleaningService<?>, TPlayerServer extends IPlayerServer, TPlayerFocusedFamilyBase extends IPlayerFocusedFamilyBase<TPlayerServer>, TTPARequest extends ITPARequest, TTPAHandler extends ITPAHandler<TTPARequest>>
        Optional<ITPAService<TTPACleaningService, TPlayerServer, TPlayerFocusedFamilyBase, TTPARequest, TTPAHandler>> tpaService();
}