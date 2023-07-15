package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.annotations.Initializer;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;

/**
 * This class should never be used directly.
 * Player-focused families offer features such as /tpa, whitelists, load-balancing, and direct connection.
 */
public abstract class PlayerFocusedServerFamily extends BaseServerFamily<PlayerServer> {
    @Initializer
    protected String parentName;

    protected WeakReference<BaseServerFamily> parent = null;
    protected LoadBalancer loadBalancer = null;
    protected String whitelist;
    protected boolean weighted;

    protected PlayerFocusedServerFamily(String name, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts, String parentName) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name);
        if(whitelist == null) this.whitelist = null;
        else this.whitelist = whitelist.getName();
        this.weighted = weighted;

        try {
            this.loadBalancer = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ignore) {}
        this.loadBalancer.setPersistence(persistence, attempts);
        this.loadBalancer.setWeighted(weighted);

        this.parentName = parentName;
    }

    public void resolveParent() {
        FamilyService familyService = VelocityRustyConnector.getAPI().getService(FAMILY_SERVICE).orElseThrow();
        BaseServerFamily family = familyService.find(parentName);
        if(family == null) {
            this.parent = new WeakReference<>(familyService.getRootFamily());
            return;
        }

        this.parent = new WeakReference<>(family);
    }
    public WeakReference<BaseServerFamily> getParent() {
        FamilyService familyService = VelocityRustyConnector.getAPI().getService(FAMILY_SERVICE).orElseThrow();
        if(familyService.getRootFamily().equals(this)) return null;
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

    public LoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }
  
    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    public Whitelist getWhitelist() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        if(this.name == null) return null;
        return api.getService(WHITELIST_SERVICE).orElseThrow().find(this.whitelist);
    }

    public long serverCount() { return this.loadBalancer.size(); }

    @Override
    public long getPlayerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
        this.loadBalancer.dump().forEach(server -> newPlayerCount.addAndGet(server.getPlayerCount()));

        return newPlayerCount.get();
    }

    @Override
    public List<PlayerServer> getRegisteredServers() {
        return this.loadBalancer.dump();
    }

    @Override
    public void addServer(PlayerServer server) {
        this.loadBalancer.add(server);
    }

    @Override
    public void removeServer(PlayerServer server) {
        this.loadBalancer.remove(server);
    }

    @Override
    public PlayerServer getServer(@NotNull ServerInfo serverInfo) {
        return this.getRegisteredServers().stream()
                .filter(server -> Objects.equals(server.getServerInfo(), serverInfo)
                ).findFirst().orElse(null);
    }

    @Override
    public void unregisterServers() throws Exception {
        ServerService serverService = VelocityRustyConnector.getAPI().getService(SERVER_SERVICE).orElseThrow();

        for (PlayerServer server : this.loadBalancer.dump()) {
            if(server == null) continue;
            serverService.unregisterServer(server.getServerInfo(),this.name, false);
        }
    }

    @Override
    public List<Player> getAllPlayers(int max) {
        List<Player> players = new ArrayList<>();

        for (PlayerServer server : this.getRegisteredServers()) {
            if(players.size() > max) break;

            players.addAll(server.getRegisteredServer().getPlayersConnected());
        }

        return players;
    }
}
