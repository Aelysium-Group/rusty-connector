package group.aelysium.rustyconnector.toolkit.velocity.whitelist;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.List;
import java.util.Optional;

public interface IWhitelistService<TWhitelist extends IWhitelist> extends Service {
    TWhitelist proxyWhitelist();

    /**
     * Add a whitelist to this manager.
     * @param whitelist The whitelist to add to this manager.
     */
    void add(TWhitelist whitelist);

    /**
     * Remove a whitelist from this manager.
     * @param whitelist The whitelist to remove from this manager.
     */
    void remove(TWhitelist whitelist);

    List<TWhitelist> dump();

    void clear();
}
