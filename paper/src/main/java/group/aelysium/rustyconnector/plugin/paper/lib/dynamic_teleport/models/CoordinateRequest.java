package group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import org.bukkit.entity.Player;

public class CoordinateRequest {
    private String clientUsername;
    private Player client = null;
    private Player target;

    public CoordinateRequest(String clientUsername, Player target) {
        this.clientUsername = clientUsername;
        this.target = target;
    }


    public String clientUsername() {
        return clientUsername;
    }

    public Player client() {
        return client;
    }

    public Player target() {
        return target;
    }

    /**
     * Attempts to resolve the clientUsername into a Player.
     * This method can only succeed of the player with clientUsername is online on this server.
     * Otherwise a NullPointerException will be thrown.
     *
     * If no exception was thrown, the username was successfully resolved.
     * @throws NullPointerException If the player with `clientUsername` is not online.
     */
    public void resolveClient() {
        PaperAPI api = PaperAPI.get();
        Player client = api.paperServer().getPlayer(this.clientUsername);
        if(client == null) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");
        if(!client.isOnline()) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");

        this.client = client;
    }

    public void teleport() throws RuntimeException {
        if(this.client == null) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");
        if(!this.client.isOnline()) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");

        if(!this.target.isOnline()) throw new NullPointerException("Attempted to resolve a tpa request while the target isn't online!");

        if(PaperAPI.get().isFolia()) {
            PaperAPI.get().scheduler().scheduleSyncDelayedTask(PaperRustyConnector.getPlugin(PaperRustyConnector.class), () -> {
                this.client.teleport(this.target.getLocation());
            }, 0);
        } else {
            this.client.teleportAsync(this.target.getLocation());
        }
    }
}
