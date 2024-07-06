package group.aelysium.rustyconnector.toolkit.proxy.events.family;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;

/**
 * Represents an MCLoader being locked on this family.
 */
public class MCLoaderLockedEvent implements Event {
    protected final Particle.Flux<Family> family;
    protected final MCLoader mcLoader;

    public MCLoaderLockedEvent(Particle.Flux<Family> family, MCLoader mcLoader) {
        this.family = family;
        this.mcLoader = mcLoader;
    }

    public Particle.Flux<Family> family() {
        return family;
    }
    public MCLoader mcLoader() {
        return mcLoader;
    }
}