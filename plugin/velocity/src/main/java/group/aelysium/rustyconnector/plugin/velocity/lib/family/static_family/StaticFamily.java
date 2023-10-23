package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.lib.family.UnavailableProtocol;
import group.aelysium.rustyconnector.api.velocity.lib.family.static_family.IStaticFamily;
import group.aelysium.rustyconnector.api.velocity.lib.lang.config.LangFileMappings;
import group.aelysium.rustyconnector.api.velocity.lib.lang.config.LangService;
import group.aelysium.rustyconnector.api.velocity.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.api.velocity.lib.util.LiquidTimestamp;
import group.aelysium.rustyconnector.api.velocity.lib.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.config.StaticFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.rmi.ConnectException;
import java.util.List;
import java.util.Optional;

import static group.aelysium.rustyconnector.api.velocity.lib.util.DependencyInjector.inject;

public class StaticFamily extends PlayerFocusedServerFamily implements IStaticFamily<PlayerServer> {
    protected LiquidTimestamp homeServerExpiration;
    protected UnavailableProtocol unavailableProtocol;
    protected ResidenceDataEnclave dataEnclave;

    private StaticFamily(String name, MySQLStorage mySQLStorage, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts, UnavailableProtocol unavailableProtocol, LiquidTimestamp homeServerExpiration, String parentFamily) throws Exception {
        super(name, whitelist, clazz, weighted, persistence, attempts, parentFamily);
        this.unavailableProtocol = unavailableProtocol;
        this.homeServerExpiration = homeServerExpiration;
        this.dataEnclave = new ResidenceDataEnclave(mySQLStorage);
    }

    public UnavailableProtocol unavailableProtocol() {
        return this.unavailableProtocol;
    }

    public LiquidTimestamp homeServerExpiration() {
        return this.homeServerExpiration;
    }
    public ResidenceDataEnclave dataEnclave() {
        return this.dataEnclave;
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
    public static StaticFamily init(DependencyInjector.DI3<List<Component>, LangService, MySQLStorage> dependencies, String familyName) throws Exception {
        Tinder api = Tinder.get();
        List<Component> bootOutput = dependencies.d1();
        LangService lang = dependencies.d2();
        MySQLStorage storage = dependencies.d3();

        StaticFamilyConfig staticFamilyConfig = new StaticFamilyConfig(new File(String.valueOf(api.dataFolder()), "families/" + familyName + ".static.yml"));
        if (!staticFamilyConfig.generate(dependencies.d1(), lang, LangFileMappings.VELOCITY_STATIC_FAMILY_TEMPLATE)) {
            throw new IllegalStateException("Unable to load or create families/" + familyName + ".static.yml!");
        }
        staticFamilyConfig.register();

        Whitelist whitelist = null;
        if (staticFamilyConfig.isWhitelist_enabled()) {
            whitelist = Whitelist.init(inject(bootOutput, lang), staticFamilyConfig.getWhitelist_name());

            api.services().whitelistService().add(whitelist);
        }

        StaticFamily family = null;
        switch (Enum.valueOf(AlgorithmType.class, staticFamilyConfig.getFirstConnection_loadBalancing_algorithm())) {
            case ROUND_ROBIN -> family = new StaticFamily(
                    familyName,
                    storage,
                    whitelist,
                    RoundRobin.class,
                    staticFamilyConfig.isFirstConnection_loadBalancing_weighted(),
                    staticFamilyConfig.isFirstConnection_loadBalancing_persistence_enabled(),
                    staticFamilyConfig.getFirstConnection_loadBalancing_persistence_attempts(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_ifUnavailable(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_expiration(),
                    staticFamilyConfig.getParent_family()
            );
            case LEAST_CONNECTION -> family = new StaticFamily(
                    familyName,
                    storage,
                    whitelist,
                    LeastConnection.class,
                    staticFamilyConfig.isFirstConnection_loadBalancing_weighted(),
                    staticFamilyConfig.isFirstConnection_loadBalancing_persistence_enabled(),
                    staticFamilyConfig.getFirstConnection_loadBalancing_persistence_attempts(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_ifUnavailable(),
                    staticFamilyConfig.getConsecutiveConnections_homeServer_expiration(),
                    staticFamilyConfig.getParent_family()
            );
            case MOST_CONNECTION -> family = new StaticFamily(
                    familyName,
                    storage,
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
            family.dataEnclave().updateExpirations(staticFamilyConfig.getConsecutiveConnections_homeServer_expiration(), family);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was an issue with MySQL! " + e.getMessage());
        }

        return family;
    }
}

class StaticFamilyConnector {
    private final StaticFamily family;
    private final Player player;
    private Component postConnectionError = null;

    public StaticFamilyConnector(StaticFamily family, Player player) {
        this.family = family;
        this.player = player;
    }

    public PlayerServer connect() throws RuntimeException {
        if(this.family.loadBalancer().size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        this.validateWhitelist();

        PlayerServer server = this.establishAnyConnection();

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
            this.family.dataEnclave().save(player, server, this.family);
        } catch (Exception e) {
            Tinder.get().logger().send(Component.text("Unable to save "+ this.player.getUsername() +" home server into MySQL! Their home server will only be saved until the server shuts down, or they log out!", NamedTextColor.RED));
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
            Optional<ServerResidence> residence = this.family.dataEnclave().fetch(player, this.family);

            if(residence.isPresent()) {
                if(residence.orElseThrow().server().isPresent()) {
                    residence.orElseThrow().server().orElseThrow().connect(this.player);
                    return residence.orElseThrow().server().orElseThrow();
                }
            }
            switch (this.family.unavailableProtocol()) {
                case ASSIGN_NEW_HOME -> {
                    this.family.dataEnclave().delete(player, this.family);
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
