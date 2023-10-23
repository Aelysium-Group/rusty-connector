package group.aelysium.rustyconnector.api.velocity.lib.family;

import group.aelysium.rustyconnector.api.velocity.lib.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.api.velocity.lib.serviceable.Service;

public interface IFamilyService extends Service {
    boolean shouldCatchDisconnectingPlayers();

    void setRootFamily(IRootFamily family);

    /**
     * Get the root family of this FamilyService.
     * If root family hasn't been set, or the family it references has been garbage collected,
     * this will return `null`.
     * @return A {@link IRootFamily} or `null`
     */
    IRootFamily rootFamily();

    /**
     * Get the number of families in this {@link IFamilyService}.
     * @return {@link Integer}
     */
    int size();
}
