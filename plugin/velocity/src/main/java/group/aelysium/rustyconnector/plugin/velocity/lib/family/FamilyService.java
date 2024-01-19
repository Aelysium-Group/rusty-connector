package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FamilyService implements IFamilyService {
    private final Map<String, IFamily> families = new HashMap<>();
    private WeakReference<IRootFamily> rootFamily;
    private final boolean catchDisconnectingPlayers;

    public FamilyService(boolean catchDisconnectingPlayers) {
        this.catchDisconnectingPlayers = catchDisconnectingPlayers;
    }

    public boolean shouldCatchDisconnectingPlayers() {
        return this.catchDisconnectingPlayers;
    }

    public void setRootFamily(IRootFamily family) {
        this.families.put(family.id(), (Family) family);
        this.rootFamily = new WeakReference<>(family);
    }

    public IRootFamily rootFamily() {
        return this.rootFamily.get();
    }

    public Optional<IFamily> find(String id) {
        IFamily family = this.families.get(id);
        if(family == null) return Optional.empty();
        return Optional.of(family);
    }

    public void add(IFamily family) {
        this.families.put(family.id(), (Family) family);
    }

    public void remove(IFamily family) {
        this.families.remove(family.id());
    }

    public List<IFamily> dump() {
        return this.families.values().stream().toList();
    }

    public void clear() {
        this.families.clear();
    }

    public int size() {
        return this.families.size();
    }

    public void kill() {
        // Teardown logic for any families that need it
        for (IFamily family : this.families.values()) {
            if(family instanceof Service)
                ((Service) family).kill();
        }

        this.families.clear();
        this.rootFamily.clear();
    }
}
