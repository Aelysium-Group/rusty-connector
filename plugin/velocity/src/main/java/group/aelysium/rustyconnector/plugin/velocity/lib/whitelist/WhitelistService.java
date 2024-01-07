package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist;

import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WhitelistService implements IWhitelistService {
    private final Map<String, IWhitelist> registeredWhitelists = new HashMap<>();
    private Whitelist.Reference proxyWhitelist;

    public IWhitelist proxyWhitelist() {
        return proxyWhitelist.get();
    }

    public void setProxyWhitelist(Whitelist.Reference whitelist) {
        this.proxyWhitelist = whitelist;
    }

    public Optional<IWhitelist> find(String name) {
        IWhitelist whitelist = this.registeredWhitelists.get(name);
        if(whitelist == null) return Optional.empty();

        return Optional.of(whitelist);
    }

    /**
     * Add a whitelist to this manager.
     * @param whitelist The whitelist to add to this manager.
     */
    @Override
    public void add(IWhitelist whitelist) {
        this.registeredWhitelists.put(whitelist.name(),whitelist);
    }

    /**
     * Remove a whitelist from this manager.
     * @param whitelist The whitelist to remove from this manager.
     */
    @Override
    public void remove(IWhitelist whitelist) {
        this.registeredWhitelists.remove(whitelist.name());
    }

    @Override
    public List<IWhitelist> dump() {
        return this.registeredWhitelists.values().stream().toList();
    }

    @Override
    public void clear() {
        this.registeredWhitelists.clear();
    }

    @Override
    public void kill() {
        this.registeredWhitelists.clear();
    }
}
