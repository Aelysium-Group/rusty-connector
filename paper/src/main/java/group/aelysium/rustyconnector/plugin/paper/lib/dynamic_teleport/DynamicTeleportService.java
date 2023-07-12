package group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport;

import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models.DynamicTeleport_TPARequest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DynamicTeleportService extends Service {
    private final List<DynamicTeleport_TPARequest> requests = new ArrayList<>();

    public DynamicTeleport_TPARequest newRequest(String client_username, Player target) {
        DynamicTeleport_TPARequest tpaRequest = new DynamicTeleport_TPARequest(client_username, target);
        requests.add(tpaRequest);

        return tpaRequest;
    }

    public DynamicTeleport_TPARequest findClient(String client_username) {
        return this.requests.stream().filter(tpaRequest -> Objects.equals(tpaRequest.getClientUsername(), client_username)).findFirst().orElse(null);
    }

    public DynamicTeleport_TPARequest findTarget(Player target) {
        return this.requests.stream().filter(tpaRequest -> Objects.equals(tpaRequest.getTarget(), target)).findFirst().orElse(null);
    }

    /**
     * Removes all requests of which `player` is either a target or source.
     * @param player The player to search for.
     */
    public void removeAllPlayersRequests(Player player) {
        if(PaperRustyConnector.getAPI().isFolia()) {
            this.requests.removeIf(tpaRequest ->
                    Objects.equals(tpaRequest.getTarget(),         player)
                 || Objects.equals(tpaRequest.getClientUsername(), player.getPlayerProfile().getName())
            );
        }
    }

    public void remove(DynamicTeleport_TPARequest tpaRequest) {
        this.requests.remove(tpaRequest);
    }

    @Override
    public void kill() {
        this.requests.clear();
    }
}
