package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;

import java.util.ArrayList;
import java.util.List;

public class TPAHandler {
    private final List<TPARequest> requests = new ArrayList<>();

    public TPARequest findRequest(Player sender, Player target) {
        return this.requests.stream()
                .filter(request -> request.getSender().equals(sender) && request.getTarget().equals(target))
                .findFirst()
                .orElse(null);
    }

    public TPARequest findRequestSender(Player sender) {
        return this.requests.stream()
                .filter(request -> request.getSender().equals(sender))
                .findFirst()
                .orElse(null);
    }

    public List<TPARequest> findRequestsForTarget(Player target) {
        return this.requests.stream()
                .filter(request -> request.getTarget().equals(target))
                .toList();
    }

    public TPARequest newRequest(Player sender, Player target) {
        VelocityAPI api = VelocityAPI.get();
        TPAService tpaService = api.services().dynamicTeleportService().orElseThrow()
                                   .services().tpaService().orElseThrow();

        TPARequest tpaRequest = new TPARequest(sender, target, tpaService.getSettings().expiration());
        requests.add(tpaRequest);

        return tpaRequest;
    }

    public void remove(TPARequest request) {
        this.requests.remove(request);
    }

    public void clearExpired() {
        this.requests.stream().filter(TPARequest::expired).forEach(request -> {
            request.getSender().sendMessage(VelocityLang.TPA_REQUEST_EXPIRED.build(request.getTarget().getUsername()));
            this.requests.remove(request);
        });
    }
    public List<TPARequest> dump() {
        return this.requests;
    }

    public void decompose() {
        this.requests.clear();
    }
}
