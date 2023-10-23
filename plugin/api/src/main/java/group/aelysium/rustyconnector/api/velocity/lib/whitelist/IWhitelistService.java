package group.aelysium.rustyconnector.api.velocity.lib.whitelist;

import group.aelysium.rustyconnector.api.velocity.lib.serviceable.Service;

import java.util.List;
import java.util.Optional;

public interface IWhitelistService extends Service {
    Optional<IWhitelist> proxyWhitelist();

    void setProxyWhitelist(IWhitelist whitelist);

    /**
     * Get a whitelist via its name.
     * @param name The name of the whitelist to get.
     * @return A family.
     */
    IWhitelist find(String name);

    /**
     * Add a whitelist to this manager.
     * @param whitelist The whitelist to add to this manager.
     */
    void add(IWhitelist whitelist);

    /**
     * Remove a whitelist from this manager.
     * @param whitelist The whitelist to remove from this manager.
     */
    void remove(IWhitelist whitelist);

    List<IWhitelist> dump();

    void clear();
}
