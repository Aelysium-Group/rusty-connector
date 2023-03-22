package group.aelysium.rustyconnector.plugin.velocity.lib.tpa;

import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;

public class TPAHandler {
    private final List<TPARequest> requests = new ArrayList<>();
    private final TPASettings settings;

    public TPAHandler(TPASettings settings) {
        this.settings = settings;
    }

    public TPASettings getSettings() {
        return settings;
    }

    public TPARequest findRequest(Player sender, Player target) {
        return this.requests.stream()
                .filter(request -> request.getSender().equals(sender) && request.getTarget().equals(target))
                .findFirst()
                .orElse(null);
    }

    public TPARequest newRequest(Player sender, Player target) {
        TPARequest tpaRequest = new TPARequest(sender, target);
        requests.add(tpaRequest);

        return tpaRequest;
    }

    public void remove(TPARequest request) {
        this.requests.remove(request);
    }
}
