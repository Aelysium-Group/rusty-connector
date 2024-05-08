package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.hub;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

public interface IHubService extends Service {
    /**
     * Checks if a particular family has "/hub" enabled.
     * @param familyName The name of the family to check
     * @return {@link Boolean}
     */
    boolean isEnabled(String familyName);
}
