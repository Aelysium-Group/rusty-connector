package group.aelysium.rustyconnector.plugin.velocity.lib.managers;

import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyManager implements NodeManager<BaseServerFamily> {
    private final Map<String, BaseServerFamily> registeredFamilies = new HashMap<>();

    /**
     * Get a family via its name.
     * @param name The name of the family to get.
     * @return A family or `null` if there is no family with the defined name.
     */
    @Override
    public BaseServerFamily find(String name) {
        return this.registeredFamilies.get(name);
    }

    /**
     * Add a family to this manager.
     * @param family The family to add to this manager.
     */
    @Override
    public void add(BaseServerFamily family) {
        this.registeredFamilies.put(family.getName(),family);
    }

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    @Override
    public void remove(BaseServerFamily family) {
        this.registeredFamilies.remove(family.getName());
    }

    @Override
    public List<BaseServerFamily> dump() {
        return this.registeredFamilies.values().stream().toList();
    }

    @Override
    public void clear() {
        this.registeredFamilies.clear();
    }

    public int size() {
        return this.registeredFamilies.size();
    }
}
