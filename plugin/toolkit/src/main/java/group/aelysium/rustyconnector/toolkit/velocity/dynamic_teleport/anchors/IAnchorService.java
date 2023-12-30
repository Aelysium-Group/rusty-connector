package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;

import java.util.List;
import java.util.Optional;

public interface IAnchorService extends Service {
    /**
     * Gets a family which has the anchor which is provided.
     * @param anchor The anchor to find the family of.
     * @return {@link Optional<IFamily>}
     */
    Optional<IFamily> familyOf(String anchor);

    /**
     * Creates a new anchor, which points to a family.
     * @param name The name of the anchor to register. Anchors can be referenced using `/anchor-name`
     * @param target The family that this anchor will teleport players to.
     */
    void create(String name, IFamily target);

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
    List<String> anchorsFor(IFamily target);
}
