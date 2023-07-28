package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class RootServerFamily extends ScalarServerFamily {
    private RootServerFamily(String name, Whitelist whitelist, Class<? extends LoadBalancer> clazz, boolean weighted, boolean persistence, int attempts) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, whitelist, clazz, weighted, persistence, attempts, null);
    }

    public static RootServerFamily init(String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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

        if(!scalarFamilyConfig.getParent_family().equals(""))
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("The root family isn't allowed to have a parent family defined! Ignoring..."), NamedTextColor.YELLOW));

        switch (Enum.valueOf(AlgorithmType.class, scalarFamilyConfig.getLoadBalancing_algorithm())) {
            case ROUND_ROBIN -> {
                return new RootServerFamily(
                        familyName,
                        whitelist,
                        RoundRobin.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts()
                );
            }
            case LEAST_CONNECTION -> {
                return new RootServerFamily(
                        familyName,
                        whitelist,
                        LeastConnection.class,
                        scalarFamilyConfig.isLoadBalancing_weighted(),
                        scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                        scalarFamilyConfig.getLoadBalancing_persistence_attempts()
                );
            }
            case MOST_CONNECTION -> {
                return new RootServerFamily(
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
