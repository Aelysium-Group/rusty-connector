package group.aelysium.rustyconnector.toolkit.velocity.family;

import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;

import java.util.List;

public interface IFamilyService<TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer, TRootFamily extends IRootFamily<TPlayerServer, TResolvablePlayer>, TBaseFamily extends IBaseFamily<TPlayerServer, TResolvablePlayer>> extends Service {
    boolean shouldCatchDisconnectingPlayers();

    void setRootFamily(TRootFamily family);

    /**
     * Get the root family of this FamilyService.
     * If root family hasn't been set, or the family it references has been garbage collected,
     * this will return `null`.
     * @return A {@link IRootFamily} or `null`
     */
    TRootFamily rootFamily();

    /**
     * Get the number of families in this {@link IFamilyService}.
     * @return {@link Integer}
     */
    int size();

    /**
     * Get a family via its name.
     * @param name The name of the family to get.
     * @return A family or `null` if there is no family with the defined name.
     */
    TBaseFamily find(String name);

    /**
     * Add a family to this manager.
     * @param family The family to add to this manager.
     */
    void add(TBaseFamily family);

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    void remove(TBaseFamily family);

    /**
     * Gets a list of all families in this service.
     * @return {@link List<TBaseFamily>}
     */
    List<TBaseFamily> dump();
}
