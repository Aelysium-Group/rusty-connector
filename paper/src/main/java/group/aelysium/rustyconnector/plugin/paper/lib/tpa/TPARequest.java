package group.aelysium.rustyconnector.plugin.paper.lib.tpa;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TPARequest {
    private String clientUsername;
    private Player client = null;
    private Player target;

    public TPARequest(String clientUsername, Player target) {
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
        Player client = PaperRustyConnector.getInstance().getServer().getPlayer(this.clientUsername);
        if(client == null) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");
        if(!client.isOnline()) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");

        this.client = client;
    }

    public void teleport() {
        if(this.client == null) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");
        if(!this.client.isOnline()) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");

        if(!this.target.isOnline()) throw new NullPointerException("Attempted to resolve a tpa request while the target isn't online!");

        new TeleportRunnable(this.client, this.target).runTaskLater(PaperRustyConnector.getInstance(), 0);
    }
}
