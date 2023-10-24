package group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa;

import com.velocitypowered.api.proxy.Player;

import java.util.List;

public interface ITPAHandler<TTPARequest extends ITPARequest> {
    TTPARequest findRequest(Player sender, Player target);

    TTPARequest findRequestSender(Player sender);

    List<TTPARequest> findRequestsForTarget(Player target);

    TTPARequest newRequest(Player sender, Player target);

    void remove(TTPARequest request);

    void clearExpired();

    List<TTPARequest> dump();

    void decompose();
}
