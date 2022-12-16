package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.FamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ServerFamily<LB extends PaperServerLoadBalancer> {
    private LB loadBalancer;
    private final String name;
    private String whitelist;
    protected long playerCount = 0;

    protected boolean weighted = false;

    private ServerFamily(String name, Whitelist whitelist, Class<LB> clazz, boolean weighted, boolean persistence, int attempts) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.name = name;
        if(whitelist == null) this.whitelist = null;
        else this.whitelist = whitelist.getName();
        this.weighted = weighted;

        this.loadBalancer = clazz.getDeclaredConstructor().newInstance();
        this.loadBalancer.setPersistence(persistence, attempts);
        this.loadBalancer.setWeighted(weighted);
    }

    public boolean isWeighted() {
        return weighted;
    }

    public LB getLoadBalancer() {
        System.out.print(this.loadBalancer);
        return this.loadBalancer;
    }

    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    public Whitelist getWhitelist() {
        if(this.name == null) return null;
        return VelocityRustyConnector.getInstance().getProxy().getWhitelistManager().find(this.whitelist);
    }

    public void setLoadBalancer(Class<LB> clazz, boolean weighted, boolean persistence, int attempts) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.loadBalancer = clazz.getDeclaredConstructor().newInstance();
        this.loadBalancer.setPersistence(persistence, attempts);
        this.loadBalancer.setWeighted(weighted);
    }

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
    public long getPlayerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
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
    public void connect(Player player) {
        if(this.loadBalancer.size() == 0) {
            player.disconnect(Component.text("There are no servers for you to connect to!"));
            return;
        }

        if(!(this.whitelist == null)) {
            Whitelist familyWhitelist = this.getWhitelist();

            String ip = player.getRemoteAddress().getHostString();

            WhitelistPlayer whitelistPlayer = new WhitelistPlayer(player.getUsername(), player.getUniqueId(), ip);

            if (!familyWhitelist.validate(whitelistPlayer)) {
                player.disconnect(Component.text(familyWhitelist.getMessage()));
                return;
            }
        }

        Callable<Boolean> notPersistent = () -> {
            PaperServer server = this.loadBalancer.getCurrent(); // Get the server that is currently listed as highest priority

            if(server.isFull())
                if(!player.hasPermission("rustyconnector.bypasssoftcap")) {
                    player.disconnect(Component.text("The server you're trying to connect to is full!"));
                    return false;
                }
            if(server.isMaxed()) {
                player.disconnect(Component.text("The server you're trying to connect to is full!"));
                return false;
            }

            if(!server.connect(player)) {
                player.disconnect(Component.text("There was an issue connecting you to the server!"));
                return false;
            }

            this.loadBalancer.iterate();

            return true;
        };
        Callable<Boolean> persistent = () -> {
            int attemptsLeft = this.loadBalancer.getAttempts();

            for (int attempt = 1; attempt <= attemptsLeft; attempt++) {
                VelocityRustyConnector.getInstance().logger().log("attempt: "+attempt);
                boolean isFinal = (attempt == attemptsLeft);
                PaperServer server = this.loadBalancer.getCurrent(); // Get the server that is currently listed as highest priority

                try {
                    if(server.isFull())
                        if(!player.hasPermission("rustyconnector.bypasssoftcap")) {
                            throw new RuntimeException("The server you're trying to connect to is full!");
                        }
                    if(server.isMaxed()) {
                        throw new RuntimeException("The server you're trying to connect to is full!");
                    }

                    if(server.connect(player)) break;
                    else throw new RuntimeException("Unable to connect you to the server in time!");
                } catch (Exception e) {
                    if(isFinal)
                        player.disconnect(Component.text(e.getMessage()));
                }
                this.loadBalancer.forceIterate();
            }

            return true;
        };

        if(this.loadBalancer.isPersistent() && this.loadBalancer.getAttempts() > 1) persistent.execute();
        else notPersistent.execute();
    }

    public List<PaperServer> getRegisteredServers() {
        return this.loadBalancer.dump();
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
    public PaperServer getServer(@NotNull ServerInfo serverInfo) {
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
     * Unregisters all servers from this family.
     */
    public void unregisterServers() throws Exception {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        for (PaperServer server : this.loadBalancer.dump()) {
            plugin.getProxy().unregisterServer(server.getServerInfo(),this.name);
        }
    }

    /**
     * Initializes all server families based on the configs.
     * @return A list of all server families.
     */
    public static ServerFamily<? extends PaperServerLoadBalancer> init(Proxy proxy, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();
        plugin.logger().log("Registering family: "+familyName);
        if(Objects.equals(familyName, "all")) throw new RuntimeException("You can't name a server \"all\"!");

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

            proxy.getWhitelistManager().add(whitelist);

            plugin.logger().log(familyName+" whitelist registered!");
        } else
            plugin.logger().log(familyName+" doesn't have a whitelist.");

        try {
            switch (Enum.valueOf(AlgorithmType.class, familyConfig.getLoadBalancing_algorithm())) {
                case ROUND_ROBIN -> {
                    return new ServerFamily<>(
                            familyName,
                            whitelist,
                            RoundRobin.class,
                            familyConfig.isLoadBalancing_weighted(),
                            familyConfig.isLoadBalancing_persistence_enabled(),
                            familyConfig.getLoadBalancing_persistence_attempts()
                    );
                }
                case LEAST_CONNECTION -> {
                    return new ServerFamily<>(
                            familyName,
                            whitelist,
                            LeastConnection.class,
                            familyConfig.isLoadBalancing_weighted(),
                            familyConfig.isLoadBalancing_persistence_enabled(),
                            familyConfig.getLoadBalancing_persistence_attempts()
                    );
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("The name used for "+familyName+"'s load balancer is invalid!");
        }

        throw new RuntimeException("There was an issue setting up the family: "+familyName);
    }

    /**
     * Reloads the whitelist associated with this server.
     */
    public void reloadWhitelist() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        Whitelist currentWhitelist = this.getWhitelist();
        if(!(currentWhitelist == null)) {
            plugin.getProxy().getWhitelistManager().remove(currentWhitelist);
        }

        FamilyConfig familyConfig = FamilyConfig.newConfig(
                this.name,
                new File(plugin.getDataFolder(), "families/"+this.name+".yml"),
                "velocity_family_template.yml"
        );
        if(!familyConfig.generate()) {
            throw new IllegalStateException("Unable to load or create families/"+this.name+".yml!");
        }
        familyConfig.register();

        Whitelist newWhitelist;
        if(familyConfig.isWhitelist_enabled()) {
            newWhitelist = Whitelist.init(familyConfig.getWhitelist_name());

            this.whitelist = familyConfig.getWhitelist_name();
            plugin.getProxy().getWhitelistManager().add(newWhitelist);

            plugin.logger().log("Finished reloading whitelist for "+this.name);
            return;
        }

        this.whitelist = null;
        plugin.logger().log("There is no whitelist for "+this.name);
    }
}
