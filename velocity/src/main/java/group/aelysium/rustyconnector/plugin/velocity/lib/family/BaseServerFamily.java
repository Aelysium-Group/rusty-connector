package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.Whitelist;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPAHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPASettings;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class BaseServerFamily {
    protected final LoadBalancer loadBalancer;
    protected final String name;
    protected String whitelist;
    protected long playerCount = 0;
    protected boolean weighted;
    protected TPAHandler tpaHandler;

    protected BaseServerFamily(String name, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts, TPASettings tpaSettings) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.name = name;
        if(whitelist == null) this.whitelist = null;
        else this.whitelist = whitelist.getName();
        this.weighted = weighted;

        this.loadBalancer = clazz.getDeclaredConstructor().newInstance();
        this.loadBalancer.setPersistence(persistence, attempts);
        this.loadBalancer.setWeighted(weighted);

        this.tpaHandler = new TPAHandler(tpaSettings);
    }

    public boolean isWeighted() {
        return weighted;
    }

    public LoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }

    public TPAHandler getTPAHandler() {
        return tpaHandler;
    }

    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    public Whitelist getWhitelist() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        if(this.name == null) return null;
        return api.getVirtualProcessor().getWhitelistManager().find(this.whitelist);
    }

    public long serverCount() { return this.loadBalancer.size(); }

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    public long getPlayerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
        this.loadBalancer.dump().forEach(server -> newPlayerCount.addAndGet(server.getPlayerCount()));

        playerCount = newPlayerCount.get();

        return this.playerCount;
    }
    public boolean containsServer(ServerInfo serverInfo) {
        return !(this.getServer(serverInfo) == null);
    }

    /**
     * Connect a player to this family
     * @param player The player to connect
     * @return A PlayerServer on successful connection.
     * @throws RuntimeException If the connection cannot be made.
     */
    public PlayerServer connect(Player player) {
        return null;
    }

    /**
     * Get all players in the family up to approximately `max`.
     * @param max The approximate max number of players to return.
     * @return A list of players.
     */
    public List<Player> getAllPlayers(int max) {
        List<Player> players = new ArrayList<>();

        for (PlayerServer server : this.getRegisteredServers()) {
            if(players.size() > max) break;

            players.addAll(server.getRegisteredServer().getPlayersConnected());
        }

        return players;
    }

    public List<PlayerServer> getRegisteredServers() {
        return this.loadBalancer.dump();
    }

    public String getName() {
        return this.name;
    }

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    public void addServer(PlayerServer server) {
        this.loadBalancer.add(server);
    }

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    public void removeServer(PlayerServer server) {
        this.loadBalancer.remove(server);
    }

    /**
     * Get a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     * @return A found server or `null` if there's no match.
     */
    public PlayerServer getServer(@NotNull ServerInfo serverInfo) {
        return this.getRegisteredServers().stream()
                .filter(server -> Objects.equals(server.getServerInfo(), serverInfo)
                ).findFirst().orElse(null);
    }

    /**
     * Unregisters all servers from this family.
     */
    public void unregisterServers() throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        for (PlayerServer server : this.loadBalancer.dump()) {
            if(server == null) continue;
            api.getVirtualProcessor().unregisterServer(server.getServerInfo(),this.name, false);
        }
    }

    /**
     * Reloads the whitelist associated with this server.
     */
    public void reloadWhitelist() {}
}
