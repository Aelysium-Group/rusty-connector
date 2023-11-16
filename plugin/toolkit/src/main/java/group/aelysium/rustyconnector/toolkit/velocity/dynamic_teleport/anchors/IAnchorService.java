package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;

import java.util.List;
import java.util.Optional;

public interface IAnchorService<TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer, TPlayerFocusedFamilyBase extends IPlayerFocusedFamilyBase<TPlayerServer, TResolvablePlayer>> extends Service {
    /**
     * Gets a family which has the anchor which is provided.
     * @param anchor The anchor to find the family of.
     * @return {@link Optional<IBaseFamily>}
     */
    Optional<TPlayerFocusedFamilyBase> familyOf(String anchor);

    /**
     * Creates a new anchor, which points to a family.
     * @param name The name of the anchor to register. Anchors can be referenced using `/<anchor name>`
     * @param target The family that this anchor will teleport players to.
     */
    void create(String name, TPlayerFocusedFamilyBase target);

    /**
     * Deletes an anchor.
     * @param name The name of the anchor to delete.
     */
    void delete(String name);

    /**
     * Find all anchors that point to a family.
     * @param target The family to target.
     * @return A list of anchor names.
     */
    List<String> anchorsFor(TPlayerFocusedFamilyBase target);
}
