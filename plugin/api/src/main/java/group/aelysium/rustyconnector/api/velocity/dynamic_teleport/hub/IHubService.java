package group.aelysium.rustyconnector.api.velocity.dynamic_teleport.hub;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

public interface IHubService extends Service {
    /**
     * Checks if a particular family has "/hub" enabled.
     * @param familyName The name of the family to check
     * @return {@link Boolean}
     */
    boolean isEnabled(String familyName);
}
