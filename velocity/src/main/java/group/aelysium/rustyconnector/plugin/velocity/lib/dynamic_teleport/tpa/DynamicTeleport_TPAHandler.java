package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;

import java.util.ArrayList;
import java.util.List;

public class DynamicTeleport_TPAHandler {
    private final List<DynamicTeleport_TPARequest> requests = new ArrayList<>();
    private final DynamicTeleport_TPASettings settings;

    public DynamicTeleport_TPAHandler(DynamicTeleport_TPASettings settings) {
        this.settings = settings;
    }

    public DynamicTeleport_TPASettings getSettings() {
        return settings;
    }

    public DynamicTeleport_TPARequest findRequest(Player sender, Player target) {
        return this.requests.stream()
                .filter(request -> request.getSender().equals(sender) && request.getTarget().equals(target))
                .findFirst()
                .orElse(null);
    }

    public DynamicTeleport_TPARequest findRequestSender(Player sender) {
        return this.requests.stream()
                .filter(request -> request.getSender().equals(sender))
                .findFirst()
                .orElse(null);
    }

    public List<DynamicTeleport_TPARequest> findRequestsForTarget(Player target) {
        return this.requests.stream()
                .filter(request -> request.getTarget().equals(target))
                .toList();
    }

    public DynamicTeleport_TPARequest newRequest(Player sender, Player target) {
        DynamicTeleport_TPARequest tpaRequest = new DynamicTeleport_TPARequest(sender, target, this.settings.getRequestLifetime());
        requests.add(tpaRequest);

        return tpaRequest;
    }

    public void remove(DynamicTeleport_TPARequest request) {
        this.requests.remove(request);
    }

    public void clearExpired() {
        this.requests.stream().filter(DynamicTeleport_TPARequest::expired).forEach(request -> {
            request.getSender().sendMessage(VelocityLang.TPA_REQUEST_EXPIRED.build(request.getTarget().getUsername()));
            this.requests.remove(request);
        });
    }
    public List<DynamicTeleport_TPARequest> dump() {
        return this.requests;
    }
}
