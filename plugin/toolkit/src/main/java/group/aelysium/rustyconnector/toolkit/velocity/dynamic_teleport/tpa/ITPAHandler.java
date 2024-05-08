package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.List;

public interface ITPAHandler {
    ITPARequest findRequest(IPlayer sender, IPlayer target);

    ITPARequest findRequestSender(IPlayer sender);

    List<ITPARequest> findRequestsForTarget(IPlayer target);

    ITPARequest newRequest(IPlayer sender, IPlayer target);

    void remove(ITPARequest request);

    void clearExpired();

    List<ITPARequest> dump();

    void decompose();
}
