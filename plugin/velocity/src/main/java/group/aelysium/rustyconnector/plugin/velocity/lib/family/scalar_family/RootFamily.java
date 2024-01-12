package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.LoadBalancerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
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

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.ROOT_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class RootFamily extends ScalarFamily implements IRootFamily {

    public RootFamily(ScalarFamily.Settings settings) {
        super(settings, ROOT_FAMILY_META);
    }

    public static RootFamily init(DependencyInjector.DI4<List<Component>, LangService, WhitelistService, ConfigService> deps, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
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

        ScalarFamily.Settings settings = new ScalarFamily.Settings(familyName, config.displayName(), loadBalancer, null, whitelist, new Connector(loadBalancer, whitelist));
        return new RootFamily(settings);
    }

    public static class Connector extends IFamily.Connector.Core {
        protected final IFamily.Connector.Core connector;

        protected Connector(@NotNull LoadBalancer loadBalancer, IWhitelist.Reference whitelist) {
            super(loadBalancer, whitelist);

            if(loadBalancer.persistent() && loadBalancer.attempts() > 1)
                this.connector = new ScalarFamily.Connector.Persistent(loadBalancer, whitelist);
            else
                this.connector = new ScalarFamily.Connector.Singleton(loadBalancer, whitelist);
        }

        @Override
        public Request connect(IPlayer player) {
            return this.connector.connect(player);
        }
    }
}
