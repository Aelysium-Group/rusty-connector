package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.FamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPAHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPASettings;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ServerFamily<LB extends PaperServerLoadBalancer> {
    private final LB loadBalancer;
    private final String name;
    private String whitelist;
    protected long playerCount = 0;
    protected boolean weighted;
    protected TPAHandler tpaHandler;

    private ServerFamily(String name, Whitelist whitelist, Class<LB> clazz, boolean weighted, boolean persistence, int attempts, TPASettings tpaSettings) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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

    public LB getLoadBalancer() {
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
        if(this.loadBalancer.size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        if(!(this.whitelist == null)) {
            Whitelist familyWhitelist = this.getWhitelist();

            if (!familyWhitelist.validate(player))
                throw new RuntimeException(familyWhitelist.getMessage());
        }

        Callable<PlayerServer> notPersistent = () -> {
            PlayerServer server = this.loadBalancer.getCurrent(); // Get the server that is currently listed as highest priority
            try {
                if(!server.validatePlayer(player))
                    throw new RuntimeException("The server you're trying to connect to is full!");

                if (!server.connect(player))
                    throw new RuntimeException("There was an issue connecting you to the server!");

                this.loadBalancer.iterate();

                return server;
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        };
        Callable<PlayerServer> persistent = () -> {
            int attemptsLeft = this.loadBalancer.getAttempts();

            for (int attempt = 1; attempt <= attemptsLeft; attempt++) {
                boolean isFinal = (attempt == attemptsLeft);
                PlayerServer server = this.loadBalancer.getCurrent(); // Get the server that is currently listed as highest priority

                try {
                    if(!server.validatePlayer(player))
                        throw new RuntimeException("The server you're trying to connect to is full!");

                    if(server.connect(player)) {
                        this.loadBalancer.forceIterate();
                        return server;
                    }
                    else throw new RuntimeException("Unable to connect you to the server in time!");
                } catch (Exception e) {
                    if(isFinal)
                        player.disconnect(Component.text(e.getMessage()));
                }
                this.loadBalancer.forceIterate();
            }

            throw new RuntimeException("There was an issue connecting you to the server!");
        };

        if(this.loadBalancer.isPersistent() && this.loadBalancer.getAttempts() > 1) return persistent.execute();
        else return notPersistent.execute();
    }

    /**
     * Connect a player to this family
     * @param player The player to connect
     * @param event The initial connection event of a player
     * @return A PlayerServer on successful connection.
     * @throws RuntimeException If the connection cannot be made.
     */
    public PlayerServer connect(Player player, PlayerChooseInitialServerEvent event) throws RuntimeException {
        if(this.loadBalancer.size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        if(!(this.whitelist == null)) {
            Whitelist familyWhitelist = this.getWhitelist();

            if (!familyWhitelist.validate(player))
                throw new RuntimeException(familyWhitelist.getMessage());
        }


        Callable<PlayerServer> notPersistent = () -> {
            PlayerServer server = this.loadBalancer.getCurrent(); // Get the server that is currently listed as highest priority
            try {
                if(!server.validatePlayer(player))
                    throw new RuntimeException("The server you're trying to connect to is full!");

                if (!server.connect(event))
                    throw new RuntimeException("There was an issue connecting you to the server!");

                this.loadBalancer.iterate();

                return server;
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        };
        Callable<PlayerServer> persistent = () -> {
            int attemptsLeft = this.loadBalancer.getAttempts();

            for (int attempt = 1; attempt <= attemptsLeft; attempt++) {
                boolean isFinal = (attempt == attemptsLeft);
                PlayerServer server = this.loadBalancer.getCurrent(); // Get the server that is currently listed as highest priority

                try {
                    if(!server.validatePlayer(player))
                        throw new RuntimeException("The server you're trying to connect to is full!");

                    if(server.connect(event)) {
                        this.loadBalancer.forceIterate();
                        return server;
                    }
                    else throw new RuntimeException("Unable to connect you to the server in time!");
                } catch (Exception e) {
                    if(isFinal)
                        player.disconnect(Component.text(e.getMessage()));
                }
                this.loadBalancer.forceIterate();
            }

            throw new RuntimeException("There was an issue connecting you to the server!");
        };

        if(this.loadBalancer.isPersistent() && this.loadBalancer.getAttempts() > 1) return persistent.execute();
        else return notPersistent.execute();
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
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static ServerFamily<? extends PaperServerLoadBalancer> init(VirtualProxyProcessor virtualProxyProcessor, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        logger.log("Registering family: "+familyName);

        FamilyConfig familyConfig = FamilyConfig.newConfig(
                familyName,
                new File(String.valueOf(api.getDataFolder()), "families/"+familyName+".yml"),
                "velocity_family_template.yml"
        );
        if(!familyConfig.generate()) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".yml!");
        }
        familyConfig.register();

        Whitelist whitelist = null;
        if(familyConfig.isWhitelist_enabled()) {
            whitelist = Whitelist.init(familyConfig.getWhitelist_name());

            virtualProxyProcessor.getWhitelistManager().add(whitelist);

            logger.log(familyName+" whitelist registered!");
        } else {
            logger.log(familyName + " doesn't have a whitelist.");
        }

        switch (Enum.valueOf(AlgorithmType.class, familyConfig.getLoadBalancing_algorithm())) {
            case ROUND_ROBIN -> {
                return new ServerFamily<>(
                        familyName,
                        whitelist,
                        RoundRobin.class,
                        familyConfig.isLoadBalancing_weighted(),
                        familyConfig.isLoadBalancing_persistence_enabled(),
                        familyConfig.getLoadBalancing_persistence_attempts(),
                        new TPASettings(familyConfig.isTPA_enabled(), familyConfig.shouldTPA_ignorePlayerCap(), familyConfig.getTPA_requestLifetime())
                );
            }
            case LEAST_CONNECTION -> {
                return new ServerFamily<>(
                        familyName,
                        whitelist,
                        LeastConnection.class,
                        familyConfig.isLoadBalancing_weighted(),
                        familyConfig.isLoadBalancing_persistence_enabled(),
                        familyConfig.getLoadBalancing_persistence_attempts(),
                        new TPASettings(familyConfig.isTPA_enabled(), familyConfig.shouldTPA_ignorePlayerCap(), familyConfig.getTPA_requestLifetime())
                );
            }
            default -> throw new RuntimeException("The name used for "+familyName+"'s load balancer is invalid!");
        }
    }

    /**
     * Reloads the whitelist associated with this server.
     */
    public void reloadWhitelist() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        Whitelist currentWhitelist = this.getWhitelist();
        if(!(currentWhitelist == null)) {
            api.getVirtualProcessor().getWhitelistManager().remove(currentWhitelist);
        }

        FamilyConfig familyConfig = FamilyConfig.newConfig(
                this.name,
                new File(String.valueOf(api.getDataFolder()), "families/"+this.name+".yml"),
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
            api.getVirtualProcessor().getWhitelistManager().add(newWhitelist);

            logger.log("Finished reloading whitelist for "+this.name);
            return;
        }

        this.whitelist = null;
        logger.log("There is no whitelist for "+this.name);
    }
}
