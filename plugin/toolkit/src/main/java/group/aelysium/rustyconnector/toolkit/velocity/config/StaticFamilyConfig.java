package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.UnavailableProtocol;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.Optional;

public interface StaticFamilyConfig {
    String displayName();
    IFamily.Reference getParent_family();
    String getFirstConnection_loadBalancer();
    boolean isWhitelist_enabled();
    String getWhitelist_name();
    UnavailableProtocol getConsecutiveConnections_homeServer_ifUnavailable();
    LiquidTimestamp getConsecutiveConnections_homeServer_expiration();

    /**
     * Convenience method to get the load balancer that this config uses.
     * This method is equivalent to running:
     * {@link IProxyConfigService#loadBalancer(String) ConfigService#loadBalancer(}{@link StaticFamilyConfig#getFirstConnection_loadBalancer()}{@link IProxyConfigService#loadBalancer(String) )}
     * @return LoadBalancerConfig
     */
    Optional<? extends LoadBalancerConfig> loadBalancer(IProxyConfigService service);

    /**
     * Convenience method to get the load balancer that this config uses.
     * This method is equivalent to running:
     * {@link IProxyConfigService#whitelist(String) ConfigService#whitelist(}{@link StaticFamilyConfig#getWhitelist_name()}{@link IProxyConfigService#whitelist(String) )}
     * @return LoadBalancerConfig
     */
    Optional<? extends WhitelistConfig> whitelist(IProxyConfigService service);
}
