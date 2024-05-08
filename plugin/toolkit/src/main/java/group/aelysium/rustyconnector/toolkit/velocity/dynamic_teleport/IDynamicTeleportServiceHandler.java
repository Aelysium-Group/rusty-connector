package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.hub.IHubService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.injectors.IInjectorService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAService;

import java.util.Optional;

public interface IDynamicTeleportServiceHandler extends IServiceHandler {
    Optional<? extends IAnchorService> anchor();
    Optional<? extends IHubService> hub();
    Optional<? extends ITPAService> tpa();
    Optional<? extends IInjectorService> injector();
}