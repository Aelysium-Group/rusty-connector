package group.aelysium.rustyconnector.toolkit;

import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityTinder;

public class RustyConnectorToolkit {
    private static MCLoaderTinder mcLoaderTinder = null;
    private static VelocityTinder velocityTinder = null;

    /**
     * Fetches the MCLoader API for RustyConnector.
     * @return {@link MCLoaderTinder}
     * @throws IllegalAccessError If you make this request without having access to the MCLoader version of RustyConnector.
     */
    public static MCLoaderTinder getMCLoaderAPI() throws IllegalAccessError {
        if(mcLoaderTinder == null) throw new IllegalAccessError("The RustyConnector MCLoader API is not available!");
        return mcLoaderTinder;
    }

    /**
     * Fetches the Proxy API for RustyConnector.
     * @return {@link VelocityTinder}
     * @throws IllegalAccessError If you make this request without having access to the Proxy version of RustyConnector.
     */
    public static VelocityTinder getProxyAPI() throws IllegalAccessError {
        if(velocityTinder == null) throw new IllegalAccessError("The RustyConnector Proxy API is not available!");
        return velocityTinder;
    }

    public static void register(MCLoaderTinder tinder) {
        mcLoaderTinder = tinder;
    }
    public static void register(VelocityTinder tinder) {
        velocityTinder = tinder;
    }

    public static void unregister() {
        mcLoaderTinder = null;
        velocityTinder = null;
    }
}