package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.annotations.Initializer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class should never be used directly.
 * Player-focused families offer features such as /tpa, whitelists, load-balancing, and direct connection.
 */
public abstract class PlayerFocusedServerFamily extends BaseServerFamily<PlayerServer> {
    @Initializer
    protected String parentName = null;

    protected WeakReference<BaseServerFamily> parent = null;
    protected LoadBalancer loadBalancer = null;
    protected String whitelist;
    protected boolean weighted;

    protected PlayerFocusedServerFamily(String name, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts, String parentName) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name);
        if(whitelist == null) this.whitelist = null;
        else this.whitelist = whitelist.name();
        this.weighted = weighted;

        try {
            this.loadBalancer = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ignore) {}
        this.loadBalancer.setPersistence(persistence, attempts);
        this.loadBalancer.setWeighted(weighted);

        this.parentName = parentName;
    }

    public void resolveParent() {
        FamilyService familyService = Tinder.get().services().familyService();
        BaseServerFamily family = familyService.find(parentName);

        this.parentName = null;
        if(family == null) {
            this.parent = new WeakReference<>(familyService.rootFamily());
            return;
        }

        this.parent = new WeakReference<>(family);
    }

    public WeakReference<BaseServerFamily> parent() {
        FamilyService familyService = Tinder.get().services().familyService();
        if(familyService.rootFamily().equals(this)) return null;
        return this.parent;
    }

    /**
     * Connect a player to this family
     * @param player The player to connect
     * @return A PlayerServer on successful connection.
     * @throws RuntimeException If the connection cannot be made.
     */
    public abstract PlayerServer connect(Player player);

    public boolean isWeighted() {
        return weighted;
    }

    public LoadBalancer loadBalancer() {
        return this.loadBalancer;
    }
  
    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    public Whitelist whitelist() {
        Tinder api = Tinder.get();
        if(this.name == null) return null;
        return api.services().whitelistService().find(this.whitelist);
    }

    public long serverCount() { return this.registeredServers().size(); }

    @Override
    public long playerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
        this.loadBalancer.dump().forEach(server -> newPlayerCount.addAndGet(server.playerCount()));

        return newPlayerCount.get();
    }

    @Override
    public List<PlayerServer> registeredServers() {
        List<PlayerServer> servers = new ArrayList<>();
        servers.addAll(this.loadBalancer.dump());
        servers.addAll(this.closedServers);
        return servers;
    }

    @Override
    public void addServer(PlayerServer server) {
        this.loadBalancer.add(server);
    }

    @Override
    public void removeServer(PlayerServer server) {
        this.loadBalancer.remove(server);
        this.closedServers.remove(server);
    }

    @Override
    public void openServer(PlayerServer server) {
        if (!this.closedServers.contains(server)) return;
        this.closedServers.remove(server);
        this.loadBalancer.add(server);
    }

    @Override
    public void closeServer(PlayerServer server) {
        if (!this.loadBalancer.dump().contains(server)) return;
        this.loadBalancer.remove(server);
        this.closedServers.add(server);
    }

    @Override
    public boolean isJoinable(PlayerServer server) {
        if (!this.registeredServers().contains(server)) return false;
        return !this.closedServers.contains(server);
    }

    @Override
    public PlayerServer findServer(@NotNull ServerInfo serverInfo) {
        return this.registeredServers().stream()
                .filter(server -> Objects.equals(server.serverInfo(), serverInfo)
                ).findFirst().orElse(null);
    }

    @Override
    public List<Player> allPlayers(int max) {
        List<Player> players = new ArrayList<>();

        for (PlayerServer server : this.registeredServers()) {
            if(players.size() > max) break;

            players.addAll(server.registeredServer().getPlayersConnected());
        }

        return players;
    }
}