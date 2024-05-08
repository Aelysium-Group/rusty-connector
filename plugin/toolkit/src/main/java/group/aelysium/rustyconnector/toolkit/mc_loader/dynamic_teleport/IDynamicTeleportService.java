package group.aelysium.rustyconnector.toolkit.mc_loader.dynamic_teleport;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.UUID;

public interface IDynamicTeleportService<TCoordinateRequest extends ICoordinateRequest> extends Service {
    /**
     * Creates a new {@link ICoordinateRequest} between the client and the target.
     * @param clientUsername The username of the client requesting to teleport.
     * @param target The target to be teleported to.
     * @return {@link ICoordinateRequest}
     */
    TCoordinateRequest newRequest(String clientUsername, UUID target);

    /**
     * Gets a request pertaining to the client.
     * @param clientUsername The username of the client that will teleport.
     * @return {@link ICoordinateRequest}
     */
    TCoordinateRequest findClient(String clientUsername);

    /**
     * Gets a request pertaining to the target.
     * @param target The UUID of the target that will be teleported to.
     * @return {@link ICoordinateRequest}
     */
    TCoordinateRequest findTarget(UUID target);

    /**
     * Deletes all {@link ICoordinateRequest} from the service relating to a particular player.
     * @param player The player to delete requests from.
     */
    void removeAllPlayersRequests(UUID player);

    /**
     * Removes a request from the service.
     * @param request The request to remove.
     */
    void remove(TCoordinateRequest request);
}
