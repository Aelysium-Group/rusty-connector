package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IScalarFamily;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.ConnectException;
import java.util.List;

import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class ScalarFamily extends PlayerFocusedFamily implements IScalarFamily<PlayerServer> {
    protected ScalarFamily(String name, LoadBalancer loadBalancer, String parentFamily, Whitelist whitelist) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, loadBalancer, parentFamily, whitelist);
    }

    public PlayerServer connect(Player player) throws RuntimeException {
        ScalarFamilyConnector connector = new ScalarFamilyConnector(this, player);
        return connector.connect();
    }
    public PlayerServer connect(PlayerChooseInitialServerEvent event) throws RuntimeException {
        ScalarFamilyConnector connector = new ScalarFamilyConnector(this, event);
        return connector.connect();
    }

    public PlayerServer fetchAny(Player player) throws RuntimeException {
        ScalarFamilyConnector connector = new ScalarFamilyConnector(this, player);
        return connector.fetchAny();
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static ScalarFamily init(DependencyInjector.DI2<List<Component>, LangService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();

        ScalarFamilyConfig scalarFamilyConfig = new ScalarFamilyConfig(new File(String.valueOf(api.dataFolder()), "families/"+familyName+".scalar.yml"));
        if(!scalarFamilyConfig.generate(dependencies.d1(), dependencies.d2(), LangFileMappings.VELOCITY_SCALAR_FAMILY_TEMPLATE)) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".scalar.yml!");
        }
        scalarFamilyConfig.register();

        Whitelist whitelist = null;
        if(scalarFamilyConfig.isWhitelist_enabled()) {
            whitelist = Whitelist.init(dependencies, scalarFamilyConfig.getWhitelist_name());

            api.services().whitelist().add(whitelist);
        }

        LoadBalancer loadBalancer;
        switch (Enum.valueOf(AlgorithmType.class, scalarFamilyConfig.getLoadBalancing_algorithm())) {
            case ROUND_ROBIN -> loadBalancer = new RoundRobin(
                    scalarFamilyConfig.isLoadBalancing_weighted(),
                    scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                    scalarFamilyConfig.getLoadBalancing_persistence_attempts()
                    );
            case LEAST_CONNECTION -> loadBalancer = new LeastConnection(
                    scalarFamilyConfig.isLoadBalancing_weighted(),
                    scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                    scalarFamilyConfig.getLoadBalancing_persistence_attempts()
            );
            case MOST_CONNECTION -> loadBalancer = new MostConnection(
                    scalarFamilyConfig.isLoadBalancing_weighted(),
                    scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                    scalarFamilyConfig.getLoadBalancing_persistence_attempts()
            );
            default -> throw new RuntimeException("The name used for "+familyName+"'s load balancer is invalid!");
        }

        return new ScalarFamily(
                familyName,
                loadBalancer,
                scalarFamilyConfig.getParent_family(),
                whitelist
        );
    }
}

class ScalarFamilyConnector {
    private final ScalarFamily family;
    private final Player player;
    private final PlayerChooseInitialServerEvent event;

    public ScalarFamilyConnector(ScalarFamily family, Player player) {
        this.family = family;
        this.player = player;
        this.event = null;
    }
    public ScalarFamilyConnector(ScalarFamily family, PlayerChooseInitialServerEvent event) {
        this.family = family;
        this.player = event.getPlayer();
        this.event = event;
    }

    public PlayerServer connect() throws RuntimeException {
        if(this.family.loadBalancer().size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        this.validateWhitelist();

        return this.establishAnyConnection();
    }

    public PlayerServer fetchAny() throws RuntimeException {
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

    public PlayerServer establishAnyConnection() {
        PlayerServer server;
        if(this.family.loadBalancer().persistent() && this.family.loadBalancer().attempts() > 1)
            server = this.connectPersistent();
        else
            server = this.connectSingleton();

        return server;
    }

    private PlayerServer connectSingleton() {
        PlayerServer server = this.family.loadBalancer().current(); // Get the server that is currently listed as highest priority
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

    private PlayerServer connectPersistent() {
        int attemptsLeft = this.family.loadBalancer().attempts();

        for (int attempt = 1; attempt <= attemptsLeft; attempt++) {
            boolean isFinal = (attempt == attemptsLeft);
            PlayerServer server = this.family.loadBalancer().current(); // Get the server that is currently listed as highest priority

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
