package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;

import java.util.Optional;

public interface IProxyConfigService extends IConfigService {
    /**
     * Returns the root `config.yml`
     */
    DefaultConfig root();
    FamiliesConfig families();
    FriendsConfig friends();
    DataTransitConfig dataTransit();
    DynamicTeleportConfig dynamicTeleport();
    LoggerConfig logger();
    PartyConfig party();

    /**
     * Gets the specified magic config.
     * @param name The name of the magic config. Shouldn't contain the file extension.
     * @return {@link Optional<MagicMCLoaderConfig>}
     */
    Optional<? extends MagicMCLoaderConfig> magicMCLoaderConfig(String name);

    /**
     * Gets the specified load balancer config.
     * @param name The name of the load balancer. Shouldn't contain the file extension.
     * @return {@link Optional<LoadBalancerConfig>}
     */
    Optional<? extends LoadBalancerConfig> loadBalancer(String name);

    /**
     * Gets the specified matchmaker config.
     * @param name The name of the matchmaker. Shouldn't contain the file extension.
     * @return {@link Optional<MatchMakerConfig>}
     */
    Optional<? extends MatchMakerConfig> matchmaker(String name);

    /**
     * Gets the specified whitelist config.
     * @param name The name of the matchmaker. Shouldn't contain the file extension.
     * @return {@link Optional<WhitelistConfig>}
     */
    Optional<? extends WhitelistConfig> whitelist(String name);

    /**
     * Gets the specified Scalar Family config.
     * @param id The id of the family. Shouldn't contain the file extension, nor `.scalar`. should only be the family's id.
     * @return {@link Optional<ScalarFamilyConfig>}
     */
    Optional<? extends ScalarFamilyConfig> scalarFamily(String id);

    /**
     * Gets the specified Static Family config.
     * @param id The id of the family. Shouldn't contain the file extension, nor `.static`. should only be the family's id.
     * @return {@link Optional<LoadBalancerConfig>}
     */
    Optional<? extends StaticFamilyConfig> staticFamily(String id);

    /**
     * Gets the specified Ranked Family config.
     * @param id The id of the family. Shouldn't contain the file extension, nor `.ranked`. should only be the family's id.
     * @return {@link Optional<RankedFamilyConfig>}
     */
    Optional<? extends RankedFamilyConfig> rankedFamily(String id);
}