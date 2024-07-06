package group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader;

import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.IFamily;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;

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