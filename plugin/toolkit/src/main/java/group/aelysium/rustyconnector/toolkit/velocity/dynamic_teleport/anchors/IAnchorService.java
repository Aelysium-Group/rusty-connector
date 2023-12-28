package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

import java.util.List;
import java.util.Optional;

public interface IAnchorService<TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>, TFamily extends Family<TMCLoader, TPlayer, TLoadBalancer>> extends Service {
    /**
     * Gets a family which has the anchor which is provided.
     * @param anchor The anchor to find the family of.
     * @return {@link Optional< Family >}
     */
    Optional<TFamily> familyOf(String anchor);

    /**
     * Creates a new anchor, which points to a family.
     * @param name The name of the anchor to register. Anchors can be referenced using `/anchor-name`
     * @param target The family that this anchor will teleport players to.
     */
    void create(String name, TFamily target);

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
    List<String> anchorsFor(TFamily target);
}
