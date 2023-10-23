package group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family;

import group.aelysium.rustyconnector.api.velocity.lib.PluginLogger;
import group.aelysium.rustyconnector.api.velocity.lib.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.api.velocity.lib.lang.config.LangFileMappings;
import group.aelysium.rustyconnector.api.velocity.lib.lang.config.LangService;
import group.aelysium.rustyconnector.api.velocity.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.api.velocity.lib.util.DependencyInjector;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RootFamily extends ScalarFamily implements IRootFamily<PlayerServer> {
    private RootFamily(String name, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, whitelist, clazz, weighted, persistence, attempts, null);
    }

    public static RootFamily init(DependencyInjector.DI2<List<Component>, LangService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        ScalarFamilyConfig scalarFamilyConfig = new ScalarFamilyConfig(new File(String.valueOf(api.dataFolder()), "families/"+familyName+".scalar.yml"));
        if(!scalarFamilyConfig.generate(dependencies.d1(), dependencies.d2(), LangFileMappings.VELOCITY_SCALAR_FAMILY_TEMPLATE)) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".scalar.yml!");
        }
        scalarFamilyConfig.register();

        Whitelist whitelist = null;
        if(scalarFamilyConfig.isWhitelist_enabled()) {
            whitelist = Whitelist.init(dependencies, scalarFamilyConfig.getWhitelist_name());

            api.services().whitelistService().add(whitelist);
        }

        if(!scalarFamilyConfig.getParent_family().equals(""))
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build("The root family isn't allowed to have a parent family defined! Ignoring...", NamedTextColor.YELLOW));

        switch (Enum.valueOf(AlgorithmType.class, scalarFamilyConfig.getLoadBalancing_algorithm())) {
            case ROUND_ROBIN -> {
                return new RootFamily(
                        familyName,
                        whitelist,
                        RoundRobin.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts()
                );
            }
            case LEAST_CONNECTION -> {
                return new RootFamily(
                        familyName,
                        whitelist,
                        LeastConnection.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts()
                );
            }
            case MOST_CONNECTION -> {
                return new RootFamily(
                        familyName,
                        whitelist,
                        MostConnection.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts()
                );
            }
            default -> throw new RuntimeException("The name used for "+familyName+"'s load balancer is invalid!");
        }
    }
}
