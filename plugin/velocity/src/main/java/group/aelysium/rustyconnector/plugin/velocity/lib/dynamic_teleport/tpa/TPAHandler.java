package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAHandler;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPARequest;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.List;
import java.util.Vector;

public class TPAHandler implements ITPAHandler {
    private final Vector<ITPARequest> requests = new Vector<>();

    public ITPARequest findRequest(IPlayer sender, IPlayer target) {
        return this.requests.stream()
                .filter(request -> request.sender().equals(sender) && request.target().equals(target))
                .findFirst()
                .orElse(null);
    }

    public ITPARequest findRequestSender(IPlayer sender) {
        return this.requests.stream()
                .filter(request -> request.sender().equals(sender))
                .findFirst()
                .orElse(null);
    }

    public List<ITPARequest> findRequestsForTarget(IPlayer target) {
        return this.requests.stream()
                .filter(request -> request.target().equals(target))
                .toList();
    }

    public ITPARequest newRequest(IPlayer sender, IPlayer target) {
        Tinder api = Tinder.get();
        TPAService tpaService = api.services().dynamicTeleport().orElseThrow()
                                   .services().tpa().orElseThrow();

        TPARequest tpaRequest = new TPARequest(sender, target, tpaService.settings().expiration());
        requests.add(tpaRequest);

        return tpaRequest;
    }

    public void remove(ITPARequest request) {
        this.requests.remove(request);
    }

    public void clearExpired() {
        this.requests.stream().filter(ITPARequest::expired).forEach(request -> {
            request.sender().sendMessage(ProxyLang.TPA_REQUEST_EXPIRED.build(request.target().username()));
            this.requests.remove(request);
        });
    }
    public List<ITPARequest> dump() {
        return this.requests;
    }

    public void decompose() {
        this.requests.clear();
    }
}
