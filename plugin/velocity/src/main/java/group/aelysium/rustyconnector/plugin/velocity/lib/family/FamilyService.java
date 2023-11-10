package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyService implements IFamilyService<PlayerServer, RootFamily, BaseFamily> {
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

    public RootFamily rootFamily() {
        return this.rootFamily.get();
    }

    public BaseFamily find(String name) {
        return this.registeredFamilies.get(name);
    }

    public void add(BaseFamily family) {
        this.registeredFamilies.put(family.name(),family);
    }

    public void remove(BaseFamily family) {
        this.registeredFamilies.remove(family.name());
    }

    public List<BaseFamily> dump() {
        return this.registeredFamilies.values().stream().toList();
    }

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
