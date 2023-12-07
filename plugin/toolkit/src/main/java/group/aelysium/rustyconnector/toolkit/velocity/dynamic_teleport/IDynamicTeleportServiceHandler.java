package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.hub.IHubService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPACleaningService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPARequest;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAService;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.Optional;

public interface IDynamicTeleportServiceHandler extends IServiceHandler {
    <TMCLoader extends IMCLoader, TPlayer extends IPlayer, TLoadBalancer extends ILoadBalancer<TMCLoader>, TFamily extends IFamily<TMCLoader, TPlayer, TLoadBalancer>>
        Optional<IAnchorService<TMCLoader, TPlayer, TLoadBalancer, TFamily>> anchorService();
    Optional<IHubService> hubService();
    <TTPACleaningService extends ITPACleaningService<?>, TMCLoader extends IMCLoader, TPlayer extends IPlayer, TLoadBalancer extends ILoadBalancer<TMCLoader>, TFamily extends IFamily<TMCLoader, TPlayer, TLoadBalancer>, TTPARequest extends ITPARequest, TTPAHandler extends ITPAHandler<TTPARequest>>
        Optional<ITPAService<TTPACleaningService, TMCLoader, TPlayer, TLoadBalancer, TFamily, TTPARequest, TTPAHandler>> tpaService();
}