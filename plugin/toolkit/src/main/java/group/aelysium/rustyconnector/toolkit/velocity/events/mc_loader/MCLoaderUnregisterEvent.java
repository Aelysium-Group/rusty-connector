package group.aelysium.rustyconnector.toolkit.velocity.events.mc_loader;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents an MCLoader unregistering from the Proxy.
 */
public class MCLoaderUnregisterEvent implements Event {
    protected final IFamily family;
    protected final IMCLoader mcLoader;

    public MCLoaderUnregisterEvent(IFamily family, IMCLoader mcLoader) {
        this.family = family;
        this.mcLoader = mcLoader;
    }

    public IFamily family() {
        return family;
    }
    public IMCLoader mcLoader() {
        return mcLoader;
    }
}