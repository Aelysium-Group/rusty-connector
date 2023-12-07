package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.version_filter.IFamilyCategory;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FamilyService implements IFamilyService<MCLoader, Player, LoadBalancer, RootFamily, Family> {
    private final Map<String, Family> families = new HashMap<>();
    private final Map<String, IFamilyCategory<Player>> categories = new HashMap<>();
    private WeakReference<RootFamily> rootFamily;
    private final boolean catchDisconnectingPlayers;

    public FamilyService(boolean catchDisconnectingPlayers) {
        this.catchDisconnectingPlayers = catchDisconnectingPlayers;
    }

    public boolean shouldCatchDisconnectingPlayers() {
        return this.catchDisconnectingPlayers;
    }

    public void setRootFamily(RootFamily family) {
        this.families.put(family.id(), family);
        this.rootFamily = new WeakReference<>(family);
    }

    public RootFamily rootFamily() {
        return this.rootFamily.get();
    }

    protected Optional<Family> find(String id) {
        Family family = this.families.get(id);
        if(family == null) return Optional.empty();
        return Optional.of(family);
    }

    protected Optional<IFamilyCategory<Player>> findCategory(String id) {
        IFamilyCategory<Player> family = this.categories.get(id);
        if(family == null) return Optional.empty();
        return Optional.of(family);
    }

    public void add(Family family) {
        this.families.put(family.id(),family);
    }

    public void remove(Family family) {
        this.families.remove(family.id());
    }

    public void add(IFamilyCategory<Player> category) {
        this.categories.put(category.id(), category);
    }

    public void remove(IFamilyCategory<Player> category) {
        this.categories.remove(category.id());
    }

    public List<Family> dump() {
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
        for (Family family : this.families.values()) {
            if(family instanceof Service)
                ((Service) family).kill();
        }

        this.families.clear();
        this.rootFamily.clear();
    }
}
