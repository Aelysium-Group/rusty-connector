package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.LoadBalancerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyPreJoinEvent;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.UnavailableProtocol;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IStaticFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.StaticFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.STATIC_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class StaticFamily extends Family implements IStaticFamily {
    protected LiquidTimestamp homeServerExpiration;
    protected UnavailableProtocol unavailableProtocol;

    private StaticFamily(Settings settings) {
        super(settings.id(), new Family.Settings(settings.displayName(), settings.loadBalancer(), settings.parentFamily(), settings.whitelist(), new StaticFamily.Connector(settings.storageService(), settings.loadBalancer(), settings.whitelist())), STATIC_FAMILY_META);
        this.unavailableProtocol = settings.unavailableProtocol();
        this.homeServerExpiration = settings.homeServerExpiration();
    }

    public UnavailableProtocol unavailableProtocol() {
        return this.unavailableProtocol;
    }

    public LiquidTimestamp homeServerExpiration() {
        return this.homeServerExpiration;
    }

    @Override
    public Request connect(IPlayer player) {
        EventDispatch.UnSafe.fireAndForget(new FamilyPreJoinEvent(this, player));

        return ((Connector) this.settings.connector()).connect(player, this);
    }

    @Override
    public void leave(IPlayer player) {}

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     *
     * @return A list of all server families.
     */
    public static StaticFamily init(DependencyInjector.DI5<List<Component>, LangService, WhitelistService, ConfigService, StorageService> deps, String familyName) throws Exception {
        Tinder api = Tinder.get();
        List<Component> bootOutput = deps.d1();
        LangService lang = deps.d2();
        WhitelistService whitelistService = deps.d3();
        StorageService storage = deps.d5();

        StaticFamilyConfig config = StaticFamilyConfig.construct(api.dataFolder(), familyName, lang, deps.d4());

        AlgorithmType loadBalancerAlgorithm;
        LoadBalancer.Settings loadBalancerSettings;
        {
            LoadBalancerConfig loadBalancerConfig = LoadBalancerConfig.construct(api.dataFolder(), config.getFirstConnection_loadBalancer(), lang);

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

        Settings settings = new Settings(
                familyName,
                config.displayName(),
                loadBalancer,
                config.getParent_family(),
                whitelist,
                config.getConsecutiveConnections_homeServer_ifUnavailable(),
                config.getConsecutiveConnections_homeServer_expiration(),
                storage
        );
        StaticFamily family = new StaticFamily(settings);

        try {
            storage.database().residences().refreshExpirations(family);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("There was an issue with MySQL! " + e.getMessage());
        }

        return family;
    }

    public record Settings(
            String id,
            String displayName,
            LoadBalancer loadBalancer,
            Family.Reference parentFamily,
            Whitelist.Reference whitelist,
            UnavailableProtocol unavailableProtocol,
            LiquidTimestamp homeServerExpiration,
            StorageService storageService
    ) {}

    public static class Connector extends IFamily.Connector.Core {
        protected final IFamily.Connector.Core connector;
        protected final StorageService storage;

        public Connector(@NotNull StorageService storage, @NotNull LoadBalancer loadBalancer, IWhitelist.Reference whitelist) {
            super(loadBalancer, whitelist);
            this.storage = storage;

            if(loadBalancer.persistent() && loadBalancer.attempts() > 1)
                connector = new ScalarFamily.Connector.Persistent(loadBalancer, whitelist);
            else
                connector = new ScalarFamily.Connector.Singleton(loadBalancer, whitelist);
        }

        public Request connect(IPlayer player, IStaticFamily family) {
            CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
            Request request = new Request(player, result);
            try {
                try {
                    Optional<IServerResidence.MCLoaderEntry> residenceOptional = this.storage.database().residences().get(family, player);

                    // Set new residence if none exists
                    if (residenceOptional.isEmpty()) {
                        request = this.connector.connect(player);

                        if(!request.result().get().connected()) return request;

                        IMCLoader server = request.result().get().server().orElseThrow();

                        this.storage.database().residences().set(family, server, player);

                        return request;
                    }

                    IServerResidence.MCLoaderEntry residence = residenceOptional.orElseThrow();

                    ConnectionResult result1 = residence.server().connect(player).result().get(10, TimeUnit.SECONDS);
                    if(!result1.connected()) throw new NoOutputException();

                    return residence.server().connect(player);
                } catch (NoOutputException ignore) {}

                switch (family.unavailableProtocol()) {
                    case ASSIGN_NEW_HOME -> {
                        request = this.connector.connect(player);

                        if(!request.result().get().connected()) return request;

                        IMCLoader server = request.result().get().server().orElseThrow();

                        this.storage.database().residences().set(family, server, player);

                        return request;
                    }
                    case CONNECT_WITH_ERROR -> {
                        Request tempRequest = this.connector.connect(player);

                        if(!tempRequest.result().get().connected()) return tempRequest;

                        result.complete(new ConnectionResult(ConnectionResult.Status.SUCCESS, ProxyLang.MISSING_HOME_SERVER, tempRequest.result().get().server()));

                        return request;
                    }
                    case CANCEL_CONNECTION_ATTEMPT -> {
                        result.complete(ConnectionResult.failed(ProxyLang.BLOCKED_STATIC_FAMILY_JOIN_ATTEMPT));
                        return request;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            result.complete(ConnectionResult.failed(Component.text("There was an issue connecting you to the server!")));
            return request;
        }

        @Override
        public Request connect(IPlayer player) {
            throw new RuntimeException("Don't use HomeServer#connect(IPlayer)! Instead use HomeServer#connect(IPlayer, IStaticFamily)");
        }
    }
}