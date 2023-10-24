package group.aelysium.rustyconnector.api.velocity.family;

import group.aelysium.rustyconnector.api.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;

public interface IFamilyService<TPlayerServer extends IPlayerServer, TRootFamily extends IRootFamily<TPlayerServer>> extends Service {
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
}
