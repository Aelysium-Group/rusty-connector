package group.aelysium.rustyconnector.api;

import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.api.velocity.central.VelocityTinder;

public interface RustyConnectorAPI {
    MCLoaderTinder getMCLoaderAPI();
    VelocityTinder getProxyAPI();
}
