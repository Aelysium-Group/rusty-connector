package group.aelysium.rustyconnector.plugin.paper.lib.tpa;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TPAQueue {
    private final List<TPARequest> requests = new ArrayList<>();

    public TPARequest newRequest(String client_username, Player target) {
        TPARequest tpaRequest = new TPARequest(client_username, target);
        requests.add(tpaRequest);

        return tpaRequest;
    }

    public TPARequest findClient(String client_username) {
        return this.requests.stream().filter(tpaRequest -> Objects.equals(tpaRequest.getClientUsername(), client_username)).findFirst().orElse(null);
    }

    public TPARequest findTarget(Player target) {
        return this.requests.stream().filter(tpaRequest -> Objects.equals(tpaRequest.getTarget(), target)).findFirst().orElse(null);
    }

    /**
     * Removes all requests of which `player` is either a target or source.
     * @param player The player to search for.
     */
    public void removeAllPlayersRequests(Player player) {
        this.requests.stream().filter(tpaRequest ->
                Objects.equals(tpaRequest.getTarget(),         player)
             || Objects.equals(tpaRequest.getClientUsername(), player.getPlayerProfile().getName())
        ).forEach(this.requests::remove);
    }

    public void remove(TPARequest tpaRequest) {
        this.requests.remove(tpaRequest);
    }
}
