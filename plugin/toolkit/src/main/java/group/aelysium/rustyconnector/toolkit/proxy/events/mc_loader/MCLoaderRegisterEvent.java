package group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.IFamily;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;

/**
 * Represents an MCLoader successfully registering to the Proxy.
 */
public class MCLoaderRegisterEvent implements Event {
    protected final Particle.Flux<IFamily> family;
    protected final IMCLoader mcLoader;

    public MCLoaderRegisterEvent(Particle.Flux<IFamily> family, IMCLoader mcLoader) {
        this.family = family;
        this.mcLoader = mcLoader;
    }

    public Particle.Flux<IFamily> family() {
        return family;
    }
    public IMCLoader mcLoader() {
        return mcLoader;
    }
}