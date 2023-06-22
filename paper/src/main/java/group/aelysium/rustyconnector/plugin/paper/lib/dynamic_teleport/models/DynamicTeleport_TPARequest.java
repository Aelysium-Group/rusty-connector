package group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.models;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import org.bukkit.entity.Player;

public class DynamicTeleport_TPARequest {
    private String clientUsername;
    private Player client = null;
    private Player target;

    public DynamicTeleport_TPARequest(String clientUsername, Player target) {
        this.clientUsername = clientUsername;
        this.target = target;
    }


    public String getClientUsername() {
        return clientUsername;
    }

    public Player getClient() {
        return client;
    }

    public Player getTarget() {
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
        PaperAPI api = PaperRustyConnector.getAPI();
        Player client = api.getServer().getPlayer(this.clientUsername);
        if(client == null) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");
        if(!client.isOnline()) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");

        this.client = client;
    }

    public void teleport() throws RuntimeException {
        if(this.client == null) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");
        if(!this.client.isOnline()) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");

        if(!this.target.isOnline()) throw new NullPointerException("Attempted to resolve a tpa request while the target isn't online!");

        this.client.teleportAsync(this.target.getLocation()).completeExceptionally(new RuntimeException("Failed to teleport "+this.client.getName()+" to "+this.target.getName()));
    }
}
