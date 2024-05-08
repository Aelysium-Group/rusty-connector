package group.aelysium.rustyconnector.toolkit.velocity.connection;

import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;

import java.util.Optional;

/**
 * The result of the connection request.
 * The returned message is always safe to send directly to the player.
 */
public record ConnectionResult(Status status, Component message, Optional<IMCLoader> server) {
    public boolean connected() {
        return this.status == Status.SUCCESS;
    }
    public static ConnectionResult failed(Component message) {
        return new ConnectionResult(Status.FAILED, message, Optional.empty());
    }
    public static ConnectionResult success(Component message, IMCLoader server) {
        if(server == null) return new ConnectionResult(Status.SUCCESS, message, Optional.empty());
        return new ConnectionResult(Status.SUCCESS, message, Optional.of(server));
    }

    public enum Status {
        FAILED,
        SUCCESS
    }
}