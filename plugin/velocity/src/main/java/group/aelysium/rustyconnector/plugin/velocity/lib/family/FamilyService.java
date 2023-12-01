package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.IConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.family.IRootConnectable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FamilyService implements IFamilyService<MCLoader, Player, Family> {
    private final Map<String, IConnectable<MCLoader, Player>> registeredFamilies = new HashMap<>();
    private WeakReference<IRootConnectable<MCLoader, Player>> rootFamily;
    private final boolean catchDisconnectingPlayers;

    public FamilyService(boolean catchDisconnectingPlayers) {
        this.catchDisconnectingPlayers = catchDisconnectingPlayers;
    }

    public boolean shouldCatchDisconnectingPlayers() {
        return this.catchDisconnectingPlayers;
    }

    public void setRootFamily(IRootConnectable<MCLoader, Player> family) {
        this.registeredFamilies.put(family.id(), family);
        this.rootFamily = new WeakReference<>(family);
    }

    public IRootConnectable<MCLoader, Player> rootFamily() {
        return this.rootFamily.get();
    }

    protected Optional<IConnectable<MCLoader, Player>> find(String name) {
        IConnectable<MCLoader, Player> family = this.registeredFamilies.get(name);
        if(family == null) return Optional.empty();
        return Optional.of(family);
    }

    public void add(IConnectable<MCLoader, Player> family) {
        this.registeredFamilies.put(family.id(),family);
    }

    public void remove(IConnectable<MCLoader, Player> family) {
        this.registeredFamilies.remove(family.id());
    }

    public List<IConnectable<MCLoader, Player>> dump() {
        return this.registeredFamilies.values().stream().toList();
    }

    public void clear() {
        this.registeredFamilies.clear();
    }

    public int size() {
        return this.registeredFamilies.size();
    }

    public void kill() {
        this.registeredFamilies.clear();
        this.rootFamily.clear();
    }
}
