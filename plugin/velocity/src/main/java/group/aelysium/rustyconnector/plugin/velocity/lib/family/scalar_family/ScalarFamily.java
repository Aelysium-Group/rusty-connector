package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.LoadBalancerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.family.Metadata;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IScalarFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.SCALAR_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class ScalarFamily extends Family implements IScalarFamily {

    public ScalarFamily(Settings settings) {
        super(settings.id(), new Family.Settings(settings.displayName(), settings.loadBalancer(), settings.parentFamily(), settings.whitelist(), settings.connector()), SCALAR_FAMILY_META);
    }

    /**
     * Used by {@link RootFamily}.
     */
    protected ScalarFamily(Settings settings, Metadata metadata) {
        super(settings.id(), new Family.Settings(settings.displayName(), settings.loadBalancer(), settings.parentFamily(), settings.whitelist(), settings.connector()), metadata);
    }

    @Override
    public void leave(IPlayer player) {}

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

        Family.Connector.Core connector = new Connector.Singleton(loadBalancer, whitelist);
        if(loadBalancer.persistent() && loadBalancer.attempts() > 1)
            connector = new Connector.Persistent(loadBalancer, whitelist);

        Settings settings = new Settings(familyName, config.displayName(), loadBalancer, config.getParent_family(), whitelist, connector);
        return new ScalarFamily(settings);
    }

    public record Settings(
            String id,
            String displayName,
            LoadBalancer loadBalancer,
            Family.Reference parentFamily,
            Whitelist.Reference whitelist,
            Family.Connector.Core connector
    ) {}

    public static class Connector {
        public static class Persistent extends Family.Connector.Core {
            public Persistent(@NotNull LoadBalancer loadBalancer, IWhitelist.Reference whitelist) {
                super(loadBalancer, whitelist);
            }

            @Override
            public Request connect(IPlayer player) {
                CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
                Request request = new Request(player, result);

                if(!this.validateLoadBalancer()) {
                    result.complete(ConnectionResult.failed(Component.text("There are no servers for you to connect to!")));
                    return request;
                }
                if(!this.whitelisted(player)) {
                    result.complete(ConnectionResult.failed(Component.text(this.whitelist.message())));
                    return request;
                }

                int attemptsLeft = this.loadBalancer.attempts();

                Optional<Request> serverResponse = Optional.empty();
                for (int attempt = 1; attempt <= attemptsLeft; attempt++) {
                    IMCLoader server = this.loadBalancer.current().orElse(null);
                    if(server == null) {
                        result.complete(ConnectionResult.failed(Component.text("There are no servers for you to connect to!")));
                        return request;
                    }

                    serverResponse = Optional.of(server.connect(player));
                    this.loadBalancer.forceIterate();
                }

                return serverResponse.orElse(request);
            }
        }

        public static class Singleton extends Family.Connector.Core {
            public Singleton(@NotNull LoadBalancer loadBalancer, IWhitelist.Reference whitelist) {
                super(loadBalancer, whitelist);
            }

            @Override
            public Request connect(IPlayer player) {
                CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
                Request request = new Request(player, result);

                if(!this.validateLoadBalancer()) {
                    result.complete(ConnectionResult.failed(Component.text("There are no servers for you to connect to!")));
                    return request;
                }
                if(!this.whitelisted(player)) {
                    result.complete(ConnectionResult.failed(Component.text(this.whitelist.message())));
                    return request;
                }
                IMCLoader server = this.loadBalancer.current().orElse(null);
                if(server == null) {
                    result.complete(ConnectionResult.failed(Component.text("There are no server to connect to. Try again later.")));
                    return request;
                }

                Request serverResponse = server.connect(player);
                try {
                    if (serverResponse.result().get().connected())
                        this.loadBalancer.iterate();
                } catch (Exception ignore) {}

                return serverResponse;
            }
        }
    }
}
