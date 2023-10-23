package group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.models.CoordinateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DynamicTeleportService extends Service {
    private final List<CoordinateRequest> requests = new ArrayList<>();

    public CoordinateRequest newRequest(String client_username, UUID target) {
        CoordinateRequest request = new CoordinateRequest(client_username, target);
        requests.add(request);

        return request;
    }

    public CoordinateRequest findClient(String clientUsername) {
        return this.requests.stream().filter(request -> Objects.equals(request.clientUsername(), clientUsername)).findFirst().orElse(null);
    }

    public CoordinateRequest findTarget(UUID target) {
        return this.requests.stream().filter(request -> Objects.equals(request.target(), target)).findFirst().orElse(null);
    }

    /**
     * Removes all requests of which `player` is either a target or source.
     * @param player The player to search for.
     */
    public void removeAllPlayersRequests(UUID player) {
        this.requests.removeIf(request ->
                Objects.equals(request.target(),         player)
             || Objects.equals(request.clientUsername(), Plugin.getAPI().getPlayerName(player))
        );
    }

    public void remove(CoordinateRequest request) {
        this.requests.remove(request);
    }

    @Override
    public void kill() {
        this.requests.clear();
    }
}
