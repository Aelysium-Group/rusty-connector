package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.rmi.ConnectException;

public class ScalarServerFamily extends PlayerFocusedServerFamily {
    protected ScalarServerFamily(String name, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts, String parentFamily) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, whitelist, clazz, weighted, persistence, attempts, parentFamily);
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
    public static ScalarServerFamily init(String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        ScalarFamilyConfig scalarFamilyConfig = ScalarFamilyConfig.newConfig(
                familyName,
                new File(String.valueOf(api.dataFolder()), "families/"+familyName+".scalar.yml"),
                "velocity_scalar_family_template.yml"
        );
        if(!scalarFamilyConfig.generate()) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".scalar.yml!");
        }
        scalarFamilyConfig.register();

        Whitelist whitelist = null;
        if(scalarFamilyConfig.isWhitelist_enabled()) {
            whitelist = Whitelist.init(scalarFamilyConfig.getWhitelist_name());

            api.services().whitelistService().add(whitelist);
        }

        switch (Enum.valueOf(AlgorithmType.class, scalarFamilyConfig.getLoadBalancing_algorithm())) {
            case ROUND_ROBIN -> {
                return new ScalarServerFamily(
                        familyName,
                        whitelist,
                        RoundRobin.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts(),
                        scalarFamilyConfig.getParent_family()
                );
            }
            case LEAST_CONNECTION -> {
                return new ScalarServerFamily(
                        familyName,
                        whitelist,
                        LeastConnection.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts(),
                        scalarFamilyConfig.getParent_family()
                );
            }
            case MOST_CONNECTION -> {
                return new ScalarServerFamily(
                        familyName,
                        whitelist,
                        MostConnection.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts(),
                        scalarFamilyConfig.getParent_family()
                );
            }
            default -> throw new RuntimeException("The name used for "+familyName+"'s load balancer is invalid!");
        }
    }
}

class ScalarFamilyConnector {
    private final ScalarServerFamily family;
    private final Player player;
    private final PlayerChooseInitialServerEvent event;

    public ScalarFamilyConnector(ScalarServerFamily family, Player player) {
        this.family = family;
        this.player = player;
        this.event = null;
    }
    public ScalarFamilyConnector(ScalarServerFamily family, PlayerChooseInitialServerEvent event) {
        this.family = family;
        this.player = event.getPlayer();
        this.event = event;
    }

    public PlayerServer connect() throws RuntimeException {
        if(this.family.loadBalancer().size() == 0)
            throw new RuntimeException("There are no servers for you to connect to!");

        this.validateWhitelist();

        PlayerServer server = this.establishAnyConnection();

        server.playerJoined();

        return server;
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
                if (!server.connect(this.event))
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
                    if (server.connect(this.event)) {
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
