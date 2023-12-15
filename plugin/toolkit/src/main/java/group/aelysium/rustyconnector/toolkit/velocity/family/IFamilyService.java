package group.aelysium.rustyconnector.toolkit.velocity.family;

import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

import java.util.List;
import java.util.Optional;

public interface IFamilyService<TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>, TRootFamily extends RootFamily<TMCLoader, TPlayer, TLoadBalancer>, TFamily extends Family<TMCLoader, TPlayer, TLoadBalancer>> extends Service {
    boolean shouldCatchDisconnectingPlayers();

    void setRootFamily(TRootFamily family);

    /**
     * Get the root family of this FamilyService.
     * If root family hasn't been set, or the family it references has been garbage collected,
     * this will return `null`.
     * @return A {@link RootFamily} or `null`
     */
    TRootFamily rootFamily();

    /**
     * Get the number of families in this {@link IFamilyService}.
     * @return {@link Integer}
     */
    int size();

    /**
     * Finds a family based on an id.
     * An alternate route of getting a family, other than "tinder.services().family().find()", can be to use {@link Family.Reference new Family.Reference(id)}{@link Family.Reference#get() .get()}.
     * @param id The id to search for.
     * @return {@link Optional<Family>}
     */
    Optional<TFamily> find(String id);

    /**
     * Add a family to this manager.
     * @param family The family to add to this manager.
     */
    void add(TFamily family);

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    void remove(TFamily family);

    /**
     * Gets a list of all families in this service.
     * @return {@link List<TFamily>}
     */
    List<TFamily> dump();
}
