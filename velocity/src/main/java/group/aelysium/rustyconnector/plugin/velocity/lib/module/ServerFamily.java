package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.LoadBalancer;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.FamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerFamily<LB extends LoadBalancer<PaperServer>> {
    private LB loadBalancer;
    private final String name;
    private final Whitelist whitelist;
    protected int playerCount = 0;
    protected boolean weighted = false;

    public ServerFamily(String name, Whitelist whitelist, Class<LB> clazz, boolean weighted) {
        this.name = name;
        this.whitelist = whitelist;
        this.weighted = weighted;

        try {
            this.loadBalancer = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
        }
    }

    public String loadBalancerName() { return this.loadBalancer.toString(); }

    /**
     * Set's the player count of a sub-server. Then re-calculates the aggregate player count of this family.
     * Finally, re-balance the family's priority order of sub-servers.
     * @param playerCount Player count to set.
     * @param server The server to set.
     */
    public void setServerPlayerCount(int playerCount, PaperServer server) {
        server.setPlayerCount(playerCount);

        this.getPlayerCount();

    }

    public long serverCount() { return this.loadBalancer.size(); }

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    public int getPlayerCount() {
        AtomicInteger newPlayerCount = new AtomicInteger();
        this.loadBalancer.dump().forEach(server -> newPlayerCount.addAndGet(server.getPlayerCount()));

        playerCount = newPlayerCount.get();

        return this.playerCount;
    }

    public boolean containsServer(ServerInfo serverInfo) {
        return !(this.getServer(serverInfo) == null);
    }
    public boolean containsServer(String name) {
        return !(this.getServer(name) == null);
    }

    /**
     * Connect a player to this family
     * @param player The player to connect
     */
    public void connect(Player player) throws MalformedURLException {
        if(!(this.whitelist == null)) {
            String ip = player.getRemoteAddress().getHostString();

            WhitelistPlayer whitelistPlayer = new WhitelistPlayer(player.getUsername(), player.getUniqueId(), ip);

            if (!whitelist.validate(whitelistPlayer)) {
                player.disconnect(Component.text("You aren't whitelisted on this server!"));
                return;
            }
        }

        if(this.loadBalancer.size() == 0) {
            player.disconnect(Component.text("There are no servers for you to connect to!"));
            return;
        }

        PaperServer server = this.loadBalancer.getCurrent(); // Get the server that is currently listed as highest priority
        VelocityRustyConnector.getInstance().logger().log(server.toString());

        if(server.isFull())
            if(!player.hasPermission("rustyconnector.bypasssoftcap")) {
                player.disconnect(Component.text("The server you're trying to connect to is full!"));
                return;
            }
        if(server.isMaxed()) {
            player.disconnect(Component.text("The server you're trying to connect to is full!"));
            return;
        }

        server.connect(player);

        this.loadBalancer.iterate();
    }

    public List<PaperServer> getRegisteredServers() {
        return this.loadBalancer.dump();
    }

    public int getQueuedServer() {
        return this.loadBalancer.getIndex();
    }

    public String getName() {
        return this.name;
    }

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    public void addServer(PaperServer server) {
        this.loadBalancer.add(server);
    }

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    public void removeServer(PaperServer server) {
        this.loadBalancer.remove(server);
    }

    /**
     * Get a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     */
    public PaperServer getServer(ServerInfo serverInfo) {
        return this.getServer(serverInfo.getName());
    }

    /**
     * Gets a server that is a part of the family.
     * @param name The name of the server.
     * @return A found server or `null` if there's no match.
     */
    public PaperServer getServer(String name) {
        return this.getRegisteredServers().stream()
                .filter(server -> Objects.equals(server.getServerInfo().getName(), name)
                ).findFirst().orElse(null);
    }

    /**
     * Gets a server that is a part of the family.
     * @param address The address of the server to get
     */
    public PaperServer getServer(InetSocketAddress address) throws NullPointerException {
        return this.getRegisteredServers().stream()
                .filter(server ->
                        Objects.equals(server.getRegisteredServer().getServerInfo().getAddress(), address)
                ).findFirst().orElse(null);
    }

    /**
     * Initializes all server families based on the configs.
     * @return A list of all server families.
     */
    public static List<ServerFamily<? extends PaperServerLoadBalancer>> init(DefaultConfig config) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        List<ServerFamily<? extends PaperServerLoadBalancer>> families = new ArrayList<>();

        for (String familyName: config.getFamilies()) {
            FamilyConfig familyConfig = FamilyConfig.newConfig(
                    familyName,
                    new File(plugin.getDataFolder(), "families/"+familyName+".yml"),
                    "velocity_family_template.yml"
            );
            if(!familyConfig.generate()) {
                throw new IllegalStateException("Unable to load or create families/"+familyName+".yml!");
            }
            familyConfig.register();

            Whitelist whitelist = null;
            if(familyConfig.isWhitelist_enabled()) {
                whitelist = Whitelist.init(familyConfig.getWhitelist_name());
            }

            switch (Enum.valueOf(AlgorithmType.class, familyConfig.getLoadBalancing_algorithm())) {
                case ROUND_ROBIN -> families.add(
                        new ServerFamily<>(
                                familyName,
                                whitelist,
                                RoundRobin.class,
                                familyConfig.isLoadBalancing_weighted()
                        )
                );
                case LEAST_CONNECTION -> families.add(
                        new ServerFamily<>(
                                familyName,
                                whitelist,
                                LeastConnection.class,
                                familyConfig.isLoadBalancing_weighted()
                        )
                );
            }
        }

        return families;
    }
}
