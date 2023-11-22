package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyReference;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.core.lib.annotations.Initializer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseFamily implements IBaseFamily<PlayerServer, RustyPlayer> {
    protected FamilyReference parent;
    protected final List<PlayerServer> lockedServers = new ArrayList<>();
    protected LoadBalancer loadBalancer;
    protected final String name;

    protected BaseFamily(String name, LoadBalancer loadBalancer, FamilyReference parent) {
        this.name = name;
        this.loadBalancer = loadBalancer;
        this.parent = parent;
    }

    public long playerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
        this.loadBalancer.dump().forEach(server -> newPlayerCount.addAndGet(server.playerCount()));

        return newPlayerCount.get();
    }

    public long serverCount() { return this.registeredServers().size(); }

    public LoadBalancer loadBalancer() {
        return this.loadBalancer;
    }

    public BaseFamily parent() {
        return this.parent.get(true);
    }

    public String name() {
        return this.name;
    }

    public List<PlayerServer> lockedServers() { return this.lockedServers;}

    public PlayerServer findServer(@NotNull ServerInfo serverInfo) {
        return this.registeredServers().stream()
                .filter(server -> server.serverInfo().equals(serverInfo)
                ).findFirst().orElse(null);
    }

    public void addServer(PlayerServer server) {
        this.loadBalancer.add(server);
    }

    public void removeServer(PlayerServer server) {
        this.loadBalancer.remove(server);
        this.lockedServers.remove(server);
    }

    public void lockServer(PlayerServer server) {
        if (!this.loadBalancer.contains(server)) return;
        this.loadBalancer.remove(server);
        this.lockedServers.add(server);

        this.loadBalancer.completeSort();
    }
    public void unlockServer(PlayerServer server) {
        if (!this.lockedServers.contains(server)) return;
        this.lockedServers.remove(server);
        this.loadBalancer.add(server);

        this.loadBalancer.completeSort();
    }
    public boolean joinable(PlayerServer server) {
        if (!this.registeredServers().contains(server)) return false;
        return !this.lockedServers.contains(server);
    }

    public List<Player> players(int max) {
        List<Player> players = new ArrayList<>();

        for (PlayerServer server : this.registeredServers()) {
            if(players.size() > max) break;

            players.addAll(server.registeredServer().getPlayersConnected());
        }

        return players;
    }


    public List<PlayerServer> registeredServers() {
        List<PlayerServer> servers = new ArrayList<>();
        servers.addAll(this.loadBalancer.dump());
        servers.addAll(this.lockedServers);
        return servers;
    }

    public boolean containsServer(ServerInfo serverInfo) {
        return !(this.findServer(serverInfo) == null);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseFamily that = (BaseFamily) o;
        return Objects.equals(name, that.name);
    }
}
