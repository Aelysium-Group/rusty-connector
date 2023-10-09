package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WhitelistService extends Service implements NodeManager<Whitelist> {
    private final Map<String, Whitelist> registeredWhitelists = new HashMap<>();
    private WeakReference<Whitelist> proxyWhitelist;

    public Optional<Whitelist> proxyWhitelist() {
        try {
            Whitelist whitelist = this.proxyWhitelist.get();
            if(whitelist != null) return Optional.of(whitelist);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public void setProxyWhitelist(Whitelist whitelist) {
        this.proxyWhitelist = new WeakReference<>(whitelist);
    }

    /**
     * Get a whitelist via its name.
     * @param name The name of the whitelist to get.
     * @return A family.
     */
    @Override
    public Whitelist find(String name) {
        if(name == null) return null;
        return this.registeredWhitelists.get(name);
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
        this.proxyWhitelist.clear();
    }
}
