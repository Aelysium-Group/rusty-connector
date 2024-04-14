package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.injectors;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;

import java.util.List;
import java.util.Optional;

public interface IInjectorService extends Service {
    /**
     * Gets a family which has the host which is provided.
     * @param host The host to find the family of.
     * @return {@link Optional<IFamily>}
     */
    Optional<IFamily> familyOf(String host);

    /**
     * Creates a new anchor, which points to a family.
     * @param host The host to register.
     * @param target The family that this anchor will teleport players to.
     */
    void create(String host, IFamily target);

    /**
     * Deletes a host.
     * @param host The host to delete.
     */
    void delete(String host);

    /**
     * Find all injectors that point to a family.
     * @param target The family to target.
     * @return A list of anchor names.
     */
    List<String> injectorsFor(IFamily target);
}
