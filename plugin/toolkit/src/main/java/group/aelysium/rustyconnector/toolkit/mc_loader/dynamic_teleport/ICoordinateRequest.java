package group.aelysium.rustyconnector.toolkit.mc_loader.dynamic_teleport;

import java.util.Optional;
import java.util.UUID;

public interface ICoordinateRequest {
    /**
     * Gets the username of the client that's attempting to teleport.
     * @return {@link String}
     */
    String clientUsername();

    /**
     * Gets the UUID of the client that's attempting to teleport.
     * This optional will be empty until you use {@link ICoordinateRequest#resolveClient()}
     * @return {@link Optional<UUID>}
     */
    Optional<UUID> client();

    /**
     * Gets the UUID of the target that's being teleport to.
     * @return {@link UUID}
     */
    UUID target();

    /**
     * Attempts to resolve the clientUsername into a Player.
     * This method can only succeed of the player with clientUsername is online on this server.
     * Otherwise, a NullPointerException will be thrown.
     * <p>
     * If no exception was thrown, the username was successfully resolved.
     * @throws NullPointerException If the player with `clientUsername` is not online.
     */
    void resolveClient() throws NullPointerException;

    /**
     * Attempts to teleport
     * @throws NullPointerException
     */
    void teleport() throws NullPointerException;
}
