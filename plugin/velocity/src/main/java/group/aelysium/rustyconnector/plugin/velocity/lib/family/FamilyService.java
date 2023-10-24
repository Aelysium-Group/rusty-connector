package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.api.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyService implements IFamilyService {
    private final Map<String, BaseFamily> registeredFamilies = new HashMap<>();
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
    public BaseFamily<?> find(String name) {
        return this.registeredFamilies.get(name);
    }

    /**
     * Add a family to this manager.
     * @param family The family to add to this manager.
     */
    @Override
    public void add(BaseFamily<?> family) {
        this.registeredFamilies.put(family.name(),family);
    }

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    @Override
    public void remove(BaseFamily<?> family) {
        this.registeredFamilies.remove(family.name());
    }

    @Override
    public List<BaseFamily<?>> dump() {
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
