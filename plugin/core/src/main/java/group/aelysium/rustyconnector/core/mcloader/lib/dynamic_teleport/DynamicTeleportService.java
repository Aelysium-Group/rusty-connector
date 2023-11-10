package group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport;

import group.aelysium.rustyconnector.toolkit.mc_loader.dynamic_teleport.IDynamicTeleportService;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DynamicTeleportService implements IDynamicTeleportService<CoordinateRequest> {
    private final List<CoordinateRequest> requests = new ArrayList<>();

    public CoordinateRequest newRequest(String clientUsername, UUID target) {
        CoordinateRequest request = new CoordinateRequest(clientUsername, target);
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
             || Objects.equals(request.clientUsername(), TinderAdapterForCore.getTinder().getPlayerName(player))
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
