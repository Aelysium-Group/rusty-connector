package group.aelysium.rustyconnector.toolkit.proxy.events.family;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;

/**
 * Represents a family rebalancing its MCLoaders via it's load balancer.
 */
public class FamilyRebalanceEvent implements Event {
    protected final Particle.Flux<Family> family;

    public FamilyRebalanceEvent(Particle.Flux<Family> family) {
        this.family = family;
    }

    public Particle.Flux<Family> family() {
        return this.family;
    }
}