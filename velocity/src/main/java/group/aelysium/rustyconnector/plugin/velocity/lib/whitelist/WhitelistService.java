package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist;

import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhitelistService extends Service implements NodeManager<Whitelist> {
    private final Map<String, Whitelist> registeredWhitelists = new HashMap<>();
    private WeakReference<Whitelist> proxyWhitelist;

    public WhitelistService() {
        super(true);
    }

    public void setProxyWhitelist(Whitelist whitelist) {
        this.proxyWhitelist = new WeakReference<>(whitelist);
    }

    /**
     * Get the proxy whitelist of this WhitelistService.
     * If proxy whitelist hasn't been set, or the whitelist it references has been garbage collected,
     * this will return `null`.
     * @return A {@link Whitelist} or `null`
     */
    public Whitelist getProxyWhitelist() {
        return this.proxyWhitelist.get();
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
        this.registeredWhitelists.put(whitelist.getName(),whitelist);
    }

    /**
     * Remove a whitelist from this manager.
     * @param whitelist The whitelist to remove from this manager.
     */
    @Override
    public void remove(Whitelist whitelist) {
        this.registeredWhitelists.remove(whitelist.getName());
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
