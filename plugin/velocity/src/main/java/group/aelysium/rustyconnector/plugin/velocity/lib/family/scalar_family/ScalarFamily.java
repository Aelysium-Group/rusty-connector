package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.LoadBalancerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.velocity.family.Metadata;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.ConnectException;
import java.util.List;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.SCALAR_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class ScalarFamily extends Family implements group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.ScalarFamily<MCLoader, Player, LoadBalancer> {

    public ScalarFamily(Settings settings) {
        super(settings.id(), new Family.Settings(settings.displayName(), settings.loadBalancer(), settings.parentFamily(), settings.whitelist()), SCALAR_FAMILY_META);
    }

    /**
     * Used by {@link RootFamily}.
     */
    protected ScalarFamily(Settings settings, Metadata metadata) {
        super(settings.id(), new Family.Settings(settings.displayName(), settings.loadBalancer(), settings.parentFamily(), settings.whitelist()), metadata);
    }

    public MCLoader connect(Player rustyPlayer) throws RuntimeException {
        com.velocitypowered.api.proxy.Player player = rustyPlayer.resolve().orElseThrow();

        ScalarFamilyConnector connector = new ScalarFamilyConnector(this, player);
        return connector.connect();
    }
    public MCLoader connect(PlayerChooseInitialServerEvent event) throws RuntimeException {
        ScalarFamilyConnector connector = new ScalarFamilyConnector(this, event);
        return connector.connect();
    }

    public MCLoader fetchAny(Player rustyPlayer) throws RuntimeException {
        com.velocitypowered.api.proxy.Player player = rustyPlayer.resolve().orElseThrow();

        ScalarFamilyConnector connector = new ScalarFamilyConnector(this, player);
        return connector.fetchAny();
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static ScalarFamily init(DependencyInjector.DI4<List<Component>, LangService, WhitelistService, ConfigService> deps, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();
        List<Component> bootOutput = deps.d1();
        LangService lang = deps.d2();
        WhitelistService whitelistService = deps.d3();

        ScalarFamilyConfig config = ScalarFamilyConfig.construct(api.dataFolder(), familyName, lang, deps.d4());

        AlgorithmType loadBalancerAlgorithm;
        LoadBalancer.Settings loadBalancerSettings;
        {
            LoadBalancerConfig loadBalancerConfig = LoadBalancerConfig.construct(api.dataFolder(), config.loadBalancer_name(), lang);

            loadBalancerAlgorithm = loadBalancerConfig.getAlgorithm();

            loadBalancerSettings = new LoadBalancer.Settings(
                    loadBalancerConfig.isWeighted(),
                    loadBalancerConfig.isPersistence_enabled(),
                    loadBalancerConfig.getPersistence_attempts()
            );
        }

        Whitelist.Reference whitelist = null;
        if (config.isWhitelist_enabled())
            whitelist = Whitelist.init(inject(bootOutput, lang, whitelistService, deps.d4()), config.getWhitelist_name());

        LoadBalancer loadBalancer;
        switch (loadBalancerAlgorithm) {
            case ROUND_ROBIN -> loadBalancer = new RoundRobin(loadBalancerSettings);
            case LEAST_CONNECTION -> loadBalancer = new LeastConnection(loadBalancerSettings);
            case MOST_CONNECTION -> loadBalancer = new MostConnection(loadBalancerSettings);
            default -> throw new RuntimeException("The id used for "+familyName+"'s load balancer is invalid!");
        }

        Settings settings = new Settings(familyName, config.displayName(), loadBalancer, config.getParent_family(), whitelist);
        return new ScalarFamily(settings);
    }

    public record Settings(
            String id,
            Component displayName,
            LoadBalancer loadBalancer,
            Family.Reference parentFamily,
            Whitelist.Reference whitelist
    ) {}
}

class ScalarFamilyConnector {
    private final ScalarFamily family;
    private final com.velocitypowered.api.proxy.Player player;
    private final PlayerChooseInitialServerEvent event;

    public ScalarFamilyConnector(ScalarFamily family, com.velocitypowered.api.proxy.Player player) {
        this.family = family;
        this.player = player;
        this.event = null;
    }
    public ScalarFamilyConnector(ScalarFamily family, PlayerChooseInitialServerEvent event) {
        this.family = family;
        this.player = event.getPlayer();
        this.event = event;
    }

    public MCLoader connect() throws RuntimeException {
        if(this.family.loadBalancer().size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        this.validateWhitelist();

        return this.establishAnyConnection();
    }

    public MCLoader fetchAny() throws RuntimeException {
        if(this.family.loadBalancer().size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        this.validateWhitelist();

        return this.family.loadBalancer().current();
    }

    public void validateWhitelist() throws RuntimeException {
        if(!(this.family.whitelist() == null)) {
            Whitelist familyWhitelist = this.family.whitelist();

            if (!familyWhitelist.validate(this.player))
                throw new RuntimeException(familyWhitelist.message());
        }
    }

    public MCLoader establishAnyConnection() {
        MCLoader server;
        if(this.family.loadBalancer().persistent() && this.family.loadBalancer().attempts() > 1)
            server = this.connectPersistent();
        else
            server = this.connectSingleton();

        return server;
    }

    private MCLoader connectSingleton() {
        MCLoader server = this.family.loadBalancer().current(); // Get the server that is currently listed as highest priority
        try {
            if(!server.validatePlayer(player))
                throw new RuntimeException("The server you're trying to connect to is full!");

            if(this.event == null) {
                if (!server.connect(player))
                    throw new RuntimeException("There was an issue connecting you to the server!");
            } else {
                if (!server.directConnect(this.event))
                    throw new RuntimeException("There was an issue connecting you to the server!");
            }

            this.family.loadBalancer().iterate();

            return server;
        } catch (RuntimeException | ConnectException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private MCLoader connectPersistent() {
        int attemptsLeft = this.family.loadBalancer().attempts();

        for (int attempt = 1; attempt <= attemptsLeft; attempt++) {
            boolean isFinal = (attempt == attemptsLeft);
            MCLoader server = this.family.loadBalancer().current(); // Get the server that is currently listed as highest priority

            try {
                if(!server.validatePlayer(player))
                    throw new RuntimeException("The server you're trying to connect to is full!");

                if(this.event == null) {
                    if (server.connect(player)) {
                        this.family.loadBalancer().forceIterate();
                        return server;
                    }
                } else {
                    if (server.directConnect(this.event)) {
                        this.family.loadBalancer().forceIterate();
                        return server;
                    }
                }

                throw new RuntimeException("Unable to connect you to the server in time!");
            } catch (Exception e) {
                if(isFinal)
                    player.disconnect(Component.text(e.getMessage()));
            }
            this.family.loadBalancer().forceIterate();
        }

        throw new RuntimeException("There was an issue connecting you to the server!");
    }
}
