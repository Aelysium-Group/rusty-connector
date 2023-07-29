package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.StaticFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.HomeServerMappingsDatabase;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.rmi.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaticServerFamily extends PlayerFocusedServerFamily {
    List<HomeServerMapping> mappingsCache = new ArrayList<>();
    LiquidTimestamp homeServerExpiration;
    UnavailableProtocol unavailableProtocol;

    private StaticServerFamily(String name, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts, UnavailableProtocol unavailableProtocol, LiquidTimestamp homeServerExpiration, String parentFamily) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, whitelist, clazz, weighted, persistence, attempts, parentFamily);
        this.unavailableProtocol = unavailableProtocol;
        this.homeServerExpiration = homeServerExpiration;
    }

    public UnavailableProtocol unavailableProtocol() {
        return this.unavailableProtocol;
    }

    public LiquidTimestamp homeServerExpiration() {
        return this.homeServerExpiration;
    }

    /**
     * Registers a new home server for a player in this family.
     * If a home server already exists, this overwrites it.
     *
     * @param player The player to register the home server for.
     * @param server The server to register as the home server.
     * @throws SQLException If there was an issue with MySQL.
     */
    public void registerHomeServer(Player player, PlayerServer server) throws SQLException {
        HomeServerMapping mapping = new HomeServerMapping(player, server, this);

        this.mappingsCache.add(mapping);
        HomeServerMappingsDatabase.save(mapping);
    }

    /**
     * Unregisters a home server from a player in this family.
     *
     * @param player The player whose home server is to be unregistered.
     * @throws SQLException If there was an issue with MySQL.
     */
    public void unregisterHomeServer(Player player) throws SQLException {
        this.mappingsCache.removeIf(item -> item.player().equals(player) && item.family().equals(this));

        HomeServerMappingsDatabase.delete(player, this);
    }

    /**
     * If mappings related to this family have a null expiration and this family's expiration isn't null.
     * This will update the expirations.
     * @throws SQLException If there was an issue with MySQL.
     */
    public void updateMappingExpirations() throws SQLException {
        if(this.homeServerExpiration == null)
            HomeServerMappingsDatabase.updateValidExpirations(this);
        else
            HomeServerMappingsDatabase.updateNullExpirations(this);
    }

    /**
     * Delete any mappings in this server that are expired.
     * @throws SQLException If there was an issue with MySQL.
     */
    public void purgeExpiredMappings() throws SQLException {
        HomeServerMappingsDatabase.purgeExpired(this);
    }

    /**
     * Find a player's home server within this family.
     *
     * @param player The player whose home server we want to find.
     * @return A home server mapping or `null` if none can be found.
     * @throws SQLException If there was an issue with MySQL.
     */
    public HomeServerMapping findHomeServer(Player player) throws SQLException {
        HomeServerMapping mapping = this.mappingsCache.stream().filter(item -> item.player().equals(player) && item.family().equals(this)).findFirst().orElse(null);
        if (mapping != null) return mapping;

        return HomeServerMappingsDatabase.find(player, this);
    }

    /**
     * Remove a player's home server from the plugin cache.
     *
     * @param player The player whose home server is to be uncached.
     */
    public void uncacheHomeServer(Player player) {
        this.mappingsCache.removeIf(item -> item.player().equals(player) && item.family().equals(this));
    }

    @Override
    public PlayerServer connect(Player player) throws RuntimeException {
        StaticFamilyConnector connector = new StaticFamilyConnector(this, player);
        return connector.connect();
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     *
     * @return A list of all server families.
     */
    public static StaticServerFamily init(String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        StaticFamilyConfig staticFamilyConfig = StaticFamilyConfig.newConfig(
                familyName,
                new File(String.valueOf(api.dataFolder()), "families/" + familyName + ".static.yml"),
                "velocity_static_family_template.yml"
        );
        if (!staticFamilyConfig.generate()) {
            throw new IllegalStateException("Unable to load or create families/" + familyName + ".static.yml!");
        }
        staticFamilyConfig.register();

        Whitelist whitelist = null;
        if (staticFamilyConfig.isWhitelist_enabled()) {
            whitelist = Whitelist.init(staticFamilyConfig.getWhitelist_name());

            api.services().whitelistService().add(whitelist);
        }

        StaticServerFamily family = null;
        switch (Enum.valueOf(AlgorithmType.class, staticFamilyConfig.getFirstConnection_loadBalancing_algorithm())) {
            case ROUND_ROBIN -> family = new StaticServerFamily(
                    familyName,
                    whitelist,
                    RoundRobin.class,
                    staticFamilyConfig.isFirstConnection_loadBalancing_weighted(),
                    staticFamilyConfig.isFirstConnection_loadBalancing_persistence_enabled(),
                    staticFamilyConfig.getFirstConnection_loadBalancing_persistence_attempts(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_ifUnavailable(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_expiration(),
                    staticFamilyConfig.getParent_family()
            );
            case LEAST_CONNECTION -> family = new StaticServerFamily(
                    familyName,
                    whitelist,
                    LeastConnection.class,
                    staticFamilyConfig.isFirstConnection_loadBalancing_weighted(),
                    staticFamilyConfig.isFirstConnection_loadBalancing_persistence_enabled(),
                    staticFamilyConfig.getFirstConnection_loadBalancing_persistence_attempts(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_ifUnavailable(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_expiration(),
                    staticFamilyConfig.getParent_family()
            );
            case MOST_CONNECTION -> family = new StaticServerFamily(
                    familyName,
                    whitelist,
                    MostConnection.class,
                    staticFamilyConfig.isFirstConnection_loadBalancing_weighted(),
                    staticFamilyConfig.isFirstConnection_loadBalancing_persistence_enabled(),
                    staticFamilyConfig.getFirstConnection_loadBalancing_persistence_attempts(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_ifUnavailable(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_expiration(),
                    staticFamilyConfig.getParent_family()
            );
        }

        if(family == null) throw new RuntimeException("The name used for " + familyName + "'s load balancer is invalid!");

        try {
            family.updateMappingExpirations();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was an issue with MySQL! " + e.getMessage());
        }

        return family;
    }
}

class StaticFamilyConnector {
    private final StaticServerFamily family;
    private final Player player;
    private Component postConnectionError = null;

    public StaticFamilyConnector(StaticServerFamily family, Player player) {
        this.family = family;
        this.player = player;
    }

    public PlayerServer connect() throws RuntimeException {
        if(this.family.loadBalancer().size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        this.validateWhitelist();

        PlayerServer server = this.establishAnyConnection();
        if(server == null) return null;

        server.playerJoined();

        return server;
    }

    public void validateWhitelist() throws RuntimeException {
        if(!(this.family.whitelist() == null)) {
            Whitelist familyWhitelist = this.family.whitelist();

            if (!familyWhitelist.validate(this.player))
                throw new RuntimeException(familyWhitelist.message());
        }
    }

    /**
     * Establish a connection to anything that's available.
     * If a home server is available, connect to that.
     * If not, check the load balancer and route the player into the proper server.
     * @return The player server that this player was connected to.
     */
    public PlayerServer establishAnyConnection() {
        PlayerServer server;
        try {
            server = this.connectHomeServer();
        } catch (Exception ignore) {
            server = establishNewConnection(true);
        }

        /*
        try {
            PartyService partyService = VelocityAPI.get().services().partyService().orElse(null);
            if (partyService == null) throw new NoOutputException();

            Party party = partyService.find(player).orElse(null);
            if (party == null) throw new NoOutputException();

            party.connect(server);
        } catch (NoOutputException ignore) {
        } catch (Exception e) {
            VelocityAPI.get().logger().log("Issue trying to pull party with player! " + e.getMessage());
        }*/

        return server;
    }

    /**
     * Establish a new connection to the family.
     * This will ignore whether the player has a home family.
     *
     * After a connection is established, the player's home family will be set or updated.
     * @return The player server that this player was connected to.
     */
    public PlayerServer establishNewConnection(boolean shouldRegisterNew) {
        PlayerServer server;
        if(this.family.loadBalancer().persistent() && this.family.loadBalancer().attempts() > 1)
            server = this.connectPersistent();
        else
            server = this.connectSingleton();

        this.sendPostConnectErrorMessage();

        if(!shouldRegisterNew) return server;

        try {
            this.family.registerHomeServer(this.player, server);
        } catch (Exception e) {
            VelocityAPI.get().logger().send(Component.text("Unable to save "+ this.player.getUsername() +" home server into MySQL! Their home server will only be saved until the server shuts down, or they log out!", NamedTextColor.RED));
            e.printStackTrace();
        }

        return server;
    }

    /**
     * Connects the player to their home server.
     * If the player's home server is unavailable and `UnavailableProtocol.ASSIGN_NEW_HOME` is set;
     * this will call `.establishNewConnection()`
     * @return The player server that this player was connected to.
     * @throws RuntimeException If no server could be connected to.
     */
    public PlayerServer connectHomeServer() throws RuntimeException {
        try {
            HomeServerMapping mapping = this.family.findHomeServer(this.player);

            if(mapping != null) {
                mapping.server().connect(this.player);
                return mapping.server();
            }
            switch (this.family.unavailableProtocol()) {
                case ASSIGN_NEW_HOME -> {
                    this.family.unregisterHomeServer(this.player);
                    return this.establishNewConnection(true);
                }
                case CONNECT_WITH_ERROR -> {
                    this.postConnectionError = VelocityLang.MISSING_HOME_SERVER;
                    return this.establishNewConnection(false);
                }
                case CANCEL_CONNECTION_ATTEMPT -> {
                    player.sendMessage(VelocityLang.BLOCKED_STATIC_FAMILY_JOIN_ATTEMPT);
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("There was an issue connecting you to the server!");
    }

    private PlayerServer connectSingleton() {
        PlayerServer server = this.family.loadBalancer().current();
        try {
            if(!server.validatePlayer(this.player))
                throw new RuntimeException("The server you're trying to connect to is full!");

            if (!server.connect(this.player))
                throw new RuntimeException("There was an issue connecting you to the server!");

            this.family.loadBalancer().iterate();

            return server;
        } catch (RuntimeException | ConnectException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PlayerServer connectPersistent() {
        int attemptsLeft = this.family.loadBalancer().attempts();

        for (int attempt = 1; attempt <= attemptsLeft; attempt++) {
            boolean isFinal = (attempt == attemptsLeft);
            PlayerServer server = this.family.loadBalancer().current(); // Get the server that is currently listed as highest priority

            try {
                if (!server.validatePlayer(this.player))
                    throw new RuntimeException("The server you're trying to connect to is full!");

                if (server.connect(this.player)) {
                    this.family.loadBalancer().forceIterate();

                    return server;
                } else throw new RuntimeException("Unable to connect you to the server in time!");
            } catch (Exception e) {
                if (isFinal)
                    this.player.disconnect(Component.text(e.getMessage()));
            }
            this.family.loadBalancer().forceIterate();
        }

        throw new RuntimeException("Unable to connect you to the server!");
    }

    public void sendPostConnectErrorMessage() {
        if(this.postConnectionError == null) return;
        this.player.sendMessage(this.postConnectionError);
    }
}
