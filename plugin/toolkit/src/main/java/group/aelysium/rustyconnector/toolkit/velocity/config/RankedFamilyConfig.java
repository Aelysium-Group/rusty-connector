package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;

import java.util.Optional;

public interface RankedFamilyConfig {
    String displayName();
    IFamily.Reference getParent_family();
    String gameId();
    String matchmaker_name();
    boolean isWhitelist_enabled();
    String getWhitelist_name();

    /**
     * Convenience method to get the load balancer that this config uses.
     * This method is equivalent to running:
     * {@link IProxyConfigService#whitelist(String) ConfigService#whitelist(}{@link RankedFamilyConfig#getWhitelist_name()}{@link IProxyConfigService#whitelist(String) )}
     * @return LoadBalancerConfig
     */
    Optional<? extends WhitelistConfig> whitelist(IProxyConfigService service);

    /**
     * Convenience method to get the matchmaker that this config uses.
     * This method is equivalent to running:
     * {@link IProxyConfigService#matchmaker(String) ConfigService#matchmaker(}{@link RankedFamilyConfig#matchmaker_name()}{@link IProxyConfigService#matchmaker(String) )}
     * @return LoadBalancerConfig
     */
    Optional<? extends MatchMakerConfig> matchmaker(IProxyConfigService service);
}