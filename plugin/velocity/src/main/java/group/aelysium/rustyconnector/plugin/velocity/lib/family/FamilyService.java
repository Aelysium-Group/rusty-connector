package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLService;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FamilyService extends Service implements NodeManager<BaseServerFamily> {
    private final Map<String, BaseServerFamily> registeredFamilies = new HashMap<>();
    private WeakReference<RootServerFamily> rootFamily;
    private final boolean catchDisconnectingPlayers;
    private final Optional<MySQLService> mySQLService;

    public FamilyService(boolean catchDisconnectingPlayers, Optional<MySQLService> mySQLService) {
        this.catchDisconnectingPlayers = catchDisconnectingPlayers;
        this.mySQLService = mySQLService;
    }

    public Optional<MySQLService> mySQLService() {
        return this.mySQLService;
    }

    public boolean shouldCatchDisconnectingPlayers() {
        return this.catchDisconnectingPlayers;
    }

    public void setRootFamily(RootServerFamily family) {
        this.registeredFamilies.put(family.name(), family);
        this.rootFamily = new WeakReference<>(family);
    }

    /**
     * Get the root family of this FamilyService.
     * If root family hasn't been set, or the family it references has been garbage collected,
     * this will return `null`.
     * @return A {@link RootServerFamily} or `null`
     */
    public RootServerFamily rootFamily() {
        return this.rootFamily.get();
    }

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
        this.registeredFamilies.put(family.name(),family);
    }

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    @Override
    public void remove(BaseServerFamily family) {
        this.registeredFamilies.remove(family.name());
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

    /**
     * Remove any home server mappings which have been cached for a specific player.
     * @param player The player to uncache mappings for.
     */
    public void uncacheHomeServerMappings(Player player) {
        FamilyService familyService = VelocityAPI.get().services().familyService();
        List<BaseServerFamily> familyList = familyService.dump().stream().filter(family -> family instanceof StaticServerFamily).toList();
        if(familyList.size() == 0) return;

        for (BaseServerFamily family : familyList) {
            ((StaticServerFamily) family).uncacheHomeServer(player);
        }
    }

    @Override
    public void kill() {
        this.registeredFamilies.clear();
        this.rootFamily.clear();
    }
}
