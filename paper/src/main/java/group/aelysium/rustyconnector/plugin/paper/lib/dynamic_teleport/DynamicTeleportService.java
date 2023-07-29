package group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models.CoordinateRequest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DynamicTeleportService extends Service {
    private final List<CoordinateRequest> requests = new ArrayList<>();

    public CoordinateRequest newRequest(String client_username, Player target) {
        CoordinateRequest request = new CoordinateRequest(client_username, target);
        requests.add(request);

        return request;
    }

    public CoordinateRequest findClient(String clientUsername) {
        return this.requests.stream().filter(request -> Objects.equals(request.clientUsername(), clientUsername)).findFirst().orElse(null);
    }

    public CoordinateRequest findTarget(Player target) {
        return this.requests.stream().filter(request -> Objects.equals(request.target(), target)).findFirst().orElse(null);
    }

    /**
     * Removes all requests of which `player` is either a target or source.
     * @param player The player to search for.
     */
    public void removeAllPlayersRequests(Player player) {
        if(PaperAPI.get().isFolia()) {
            this.requests.removeIf(request ->
                    Objects.equals(request.target(),         player)
                 || Objects.equals(request.clientUsername(), player.getPlayerProfile().getName())
            );
        }
    }

    public void remove(CoordinateRequest request) {
        this.requests.remove(request);
    }

    @Override
    public void kill() {
        this.requests.clear();
    }
}
