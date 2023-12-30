package group.aelysium.rustyconnector.core.mcloader.lib.dynamic_teleport;

import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.dynamic_teleport.ICoordinateRequest;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;

import java.util.Optional;
import java.util.UUID;

public class CoordinateRequest implements ICoordinateRequest {

    private final String clientUsername;
    private Optional<UUID> client = Optional.empty();
    private final UUID target;

    public CoordinateRequest(String clientUsername, UUID target) {
        this.clientUsername = clientUsername;
        this.target = target;
    }


    public String clientUsername() {
        return clientUsername;
    }

    public Optional<UUID> client() {
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
        IMCLoaderTinder api = TinderAdapterForCore.getTinder();
        UUID client = api.getPlayerUUID(this.clientUsername);
        if(client == null) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");
        if(!api.isOnline(client)) throw new NullPointerException("Attempted to resolve clientUsername `"+this.clientUsername+"` while player wasn't online.");

        this.client = Optional.of(client);
    }

    public void teleport() throws RuntimeException {
        IMCLoaderTinder api = TinderAdapterForCore.getTinder();
        if(this.client.isEmpty()) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");
        if(!api.isOnline(this.client.get())) throw new NullPointerException("Attempted to resolve a tpa request while the client isn't online!");
        if(!api.isOnline(this.target)) throw new NullPointerException("Attempted to resolve a tpa request while the target isn't online!");

        api.teleportPlayer(client.get(), target);
    }
}
