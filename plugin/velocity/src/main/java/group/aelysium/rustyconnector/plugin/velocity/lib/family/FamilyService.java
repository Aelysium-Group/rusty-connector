package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.api.velocity.lib.family.IFamilyService;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyService implements IFamilyService {
    private final Map<String, BaseServerFamily> registeredFamilies = new HashMap<>();
    private WeakReference<RootFamily> rootFamily;
    private final boolean catchDisconnectingPlayers;

    public FamilyService(boolean catchDisconnectingPlayers) {
        this.catchDisconnectingPlayers = catchDisconnectingPlayers;
    }

    public boolean shouldCatchDisconnectingPlayers() {
        return this.catchDisconnectingPlayers;
    }

    public void setRootFamily(RootFamily family) {
        this.registeredFamilies.put(family.name(), family);
        this.rootFamily = new WeakReference<>(family);
    }

    /**
     * Get the root family of this FamilyService.
     * If root family hasn't been set, or the family it references has been garbage collected,
     * this will return `null`.
     * @return A {@link RootFamily} or `null`
     */
    public RootFamily rootFamily() {
        return this.rootFamily.get();
    }

    /**
     * Get a family via its name.
     * @param name The name of the family to get.
     * @return A family or `null` if there is no family with the defined name.
     */
    @Override
    public BaseServerFamily<?> find(String name) {
        return this.registeredFamilies.get(name);
    }

    /**
     * Add a family to this manager.
     * @param family The family to add to this manager.
     */
    @Override
    public void add(BaseServerFamily<?> family) {
        this.registeredFamilies.put(family.name(),family);
    }

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    @Override
    public void remove(BaseServerFamily<?> family) {
        this.registeredFamilies.remove(family.name());
    }

    @Override
    public List<BaseServerFamily<?>> dump() {
        return this.registeredFamilies.values().stream().toList();
    }

    @Override
    public void clear() {
        this.registeredFamilies.clear();
    }

    public int size() {
        return this.registeredFamilies.size();
    }

    @Override
    public void kill() {
        this.registeredFamilies.clear();
        this.rootFamily.clear();
    }
}
