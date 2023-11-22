package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.config.LoadBalancerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType.*;

public class RootFamily extends ScalarFamily implements IRootFamily<PlayerServer, RustyPlayer> {
    public RootFamily(Settings settings) {
        super(settings);
    }

    public static RootFamily init(DependencyInjector.DI2<List<Component>, LangService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

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

        Whitelist whitelist = null;
        if(config.isWhitelist_enabled()) {
            whitelist = Whitelist.init(dependencies, config.getWhitelist_name());

            api.services().whitelist().add(whitelist);
        }

        LoadBalancer loadBalancer;
        switch (loadBalancerAlgorithm) {
            case ROUND_ROBIN -> loadBalancer = new RoundRobin(loadBalancerSettings);
            case LEAST_CONNECTION -> loadBalancer = new LeastConnection(loadBalancerSettings);
            case MOST_CONNECTION -> loadBalancer = new MostConnection(loadBalancerSettings);
            default -> throw new RuntimeException("The name used for "+familyName+"'s load balancer is invalid!");
        }

        Settings settings = new ScalarFamily.Settings(familyName, loadBalancer, null, whitelist);
        return new RootFamily(settings);
    }
}
