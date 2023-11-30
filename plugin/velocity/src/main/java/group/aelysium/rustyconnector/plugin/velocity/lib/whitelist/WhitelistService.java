package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist;

import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WhitelistService implements IWhitelistService<Whitelist> {
    private final Map<String, Whitelist> registeredWhitelists = new HashMap<>();
    private Whitelist.Reference proxyWhitelist;

    public Whitelist proxyWhitelist() {
        return proxyWhitelist.get();
    }

    public void setProxyWhitelist(Whitelist.Reference whitelist) {
        this.proxyWhitelist = whitelist;
    }

    protected Optional<Whitelist> find(String name) {
        Whitelist whitelist = this.registeredWhitelists.get(name);
        if(whitelist == null) return Optional.empty();

        return Optional.of(whitelist);
    }

    /**
     * Add a whitelist to this manager.
     * @param whitelist The whitelist to add to this manager.
     */
    @Override
    public void add(Whitelist whitelist) {
        this.registeredWhitelists.put(whitelist.name(),whitelist);
    }

    /**
     * Remove a whitelist from this manager.
     * @param whitelist The whitelist to remove from this manager.
     */
    @Override
    public void remove(Whitelist whitelist) {
        this.registeredWhitelists.remove(whitelist.name());
    }

    @Override
    public List<Whitelist> dump() {
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
