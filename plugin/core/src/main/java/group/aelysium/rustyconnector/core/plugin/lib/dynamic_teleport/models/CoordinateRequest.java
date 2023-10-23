package group.aelysium.rustyconnector.core.plugin.lib.dynamic_teleport.models;

import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.plugin.Plugin;

import java.util.UUID;

public class CoordinateRequest {

    private final String clientUsername;
    private UUID client;
    private final UUID target;

    public CoordinateRequest(String clientUsername, UUID target) {
        this.clientUsername = clientUsername;
        this.target = target;
    }


    public String clientUsername() {
        return clientUsername;
    }

    public UUID client() {
        return client;
    }

    public UUID target() {
        return target;
    }

    /**
     * Attempts to resolve the clientUsername into a Player.
     * This method can only succeed of the player with clientUsername is online on this server.
     * Otherwise, a NullPointerException will be thrown.
     * <p>
     * If no exception was thrown, the username was successfully resolved.
     * @throws NullPointerException If the player with `clientUsername` is not online.
     */
    public void resolveClient() {
        Tinder api = Plugin.getAPI();
        UUID client = api.getPlayerUUID(this.clientUsername);
        if(client == null) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");
        if(!api.isOnline(client)) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");

        this.client = client;
    }

    public void teleport() throws RuntimeException {
        Tinder api = Plugin.getAPI();
        if(this.client == null) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");
        if(!api.isOnline(this.client)) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");
        if(!api.isOnline(this.target)) throw new NullPointerException("Attempted to resolve a tpa request while the target isn't online!");

        api.teleportPlayer(client, target);
    }
}
