package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;

import java.util.Optional;

public interface ScalarFamilyConfig {
    String displayName();
    IFamily.Reference getParent_family();
    String loadBalancer_name();
    boolean isWhitelist_enabled();
    String getWhitelist_name();

    /**
     * Convenience method to get the load balancer that this config uses.
     * This method is equivalent to running:
     * {@link IProxyConfigService#loadBalancer(String) ConfigService#loadBalancer(}{@link ScalarFamilyConfig#loadBalancer_name()}{@link IProxyConfigService#loadBalancer(String) )}
     * @return LoadBalancerConfig
     */
    Optional<? extends LoadBalancerConfig> loadBalancer(IProxyConfigService service);

    /**
     * Convenience method to get the load balancer that this config uses.
     * This method is equivalent to running:
     * {@link IProxyConfigService#whitelist(String) ConfigService#whitelist(}{@link ScalarFamilyConfig#getWhitelist_name()}{@link IProxyConfigService#whitelist(String) )}
     * @return LoadBalancerConfig
     */
    Optional<? extends WhitelistConfig> whitelist(IProxyConfigService service);
}