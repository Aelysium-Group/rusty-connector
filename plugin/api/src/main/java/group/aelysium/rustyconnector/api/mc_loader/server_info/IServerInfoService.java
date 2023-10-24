package group.aelysium.rustyconnector.api.mc_loader.server_info;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

public interface IServerInfoService extends Service {
    /**
     * Gets the address of this server.
     * The address, assuming the user entered it properly, should be formatted in the same format as you format a joinable address in Velocity's velocity.toml.
     * @return {@link String}
     */
    String address();

    /**
     * Gets the name of this server.
     * @return {@link String}
     */
    String name();

    /**
     * Gets the name of the family that this server should be in.
     * Whether a family with this name actually exists on the proxy is unknown to the server.
     * @return {@link String}
     */
    String family();

    /**
     * The number of players on this server.
     * @return {@link Integer}
     */
    int playerCount();

    /**
     * The weight value that this server has while being processed in the load balancer.
     * @return {@link Integer}
     */
    int weight();

    /**
     * The soft player cap of this server.
     * @return {@link Integer}
     */
    int softPlayerCap();

    /**
     * The hard player cap of this server.
     * @return {@link Integer}
     */
    int hardPlayerCap();
}
