package group.aelysium.rustyconnector.plugin.paper.central;

import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.api.velocity.central.VelocityTinder;
import group.aelysium.rustyconnector.core.plugin.Plugin;

public class RustyConnectorAPI implements group.aelysium.rustyconnector.api.RustyConnectorAPI {
    @Override
    public MCLoaderTinder getMCLoaderAPI() throws IllegalAccessError {
        return Plugin.getAPI();
    }

    @Override
    public VelocityTinder getProxyAPI() throws IllegalAccessError {
        throw new IllegalAccessError("You aren't working in an environment to access the RustyConnector MCLoader API!");
    }
}
