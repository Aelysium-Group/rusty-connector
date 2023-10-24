package group.aelysium.rustyconnector.api;

import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.api.velocity.central.VelocityTinder;

public interface RustyConnectorAPI {
    /**
     * Fetches the MCLoader API for RustyConnector.
     * @return {@link MCLoaderTinder}
     * @throws IllegalAccessError If you make this request without having access to the MCLoader version of RustyConnector.
     */
    MCLoaderTinder getMCLoaderAPI() throws IllegalAccessError;

    /**
     * Fetches the Proxy API for RustyConnector.
     * @return {@link VelocityTinder}
     * @throws IllegalAccessError If you make this request without having access to the Proxy version of RustyConnector.
     */
    VelocityTinder getProxyAPI() throws IllegalAccessError;
}
