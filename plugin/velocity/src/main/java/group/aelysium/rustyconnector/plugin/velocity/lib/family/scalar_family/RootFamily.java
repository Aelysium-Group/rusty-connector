package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.config.LoadBalancerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.ROOT_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class RootFamily extends ScalarFamily implements IRootFamily<MCLoader, Player> {

    public RootFamily(Settings settings) {
        super(settings, ROOT_FAMILY_META);
    }

    public static RootFamily init(DependencyInjector.DI3<List<Component>, LangService, WhitelistService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();
        List<Component> bootOutput = dependencies.d1();
        LangService lang = dependencies.d2();
        WhitelistService whitelistService = dependencies.d3();

        ScalarFamilyConfig config = new ScalarFamilyConfig(api.dataFolder(), familyName);
        if(!config.generate(dependencies.d1(), dependencies.d2(), LangFileMappings.VELOCITY_SCALAR_FAMILY_TEMPLATE)) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".scalar.yml!");
        }
        config.register();

        AlgorithmType loadBalancerAlgorithm;
        LoadBalancer.Settings loadBalancerSettings;
        {
            LoadBalancerConfig loadBalancerConfig = new LoadBalancerConfig(api.dataFolder(), config.loadBalancer());
            if (!loadBalancerConfig.generate(dependencies.d1(), dependencies.d2(), LangFileMappings.VELOCITY_LOAD_BALANCER_TEMPLATE)) {
                throw new IllegalStateException("Unable to load or create load_balancer/" + config.loadBalancer() + ".yml!");
            }
            loadBalancerConfig.register();

            loadBalancerAlgorithm = loadBalancerConfig.getAlgorithm();

            loadBalancerSettings = new LoadBalancer.Settings(
                    loadBalancerConfig.isWeighted(),
                    loadBalancerConfig.isPersistence_enabled(),
                    loadBalancerConfig.getPersistence_attempts()
            );
        }

        Whitelist.Reference whitelist = null;
        if (config.isWhitelist_enabled())
            whitelist = Whitelist.init(inject(bootOutput, lang, whitelistService), config.getWhitelist_name());

        LoadBalancer loadBalancer;
        switch (loadBalancerAlgorithm) {
            case ROUND_ROBIN -> loadBalancer = new RoundRobin(loadBalancerSettings);
            case LEAST_CONNECTION -> loadBalancer = new LeastConnection(loadBalancerSettings);
            case MOST_CONNECTION -> loadBalancer = new MostConnection(loadBalancerSettings);
            default -> throw new RuntimeException("The id used for "+familyName+"'s load balancer is invalid!");
        }

        Settings settings = new ScalarFamily.Settings(familyName, config.displayName(), loadBalancer, null, whitelist);
        return new RootFamily(settings);
    }
}
