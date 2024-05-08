package group.aelysium.rustyconnector.toolkit.velocity.events.family;

import group.aelysium.rustyconnector.toolkit.core.events.Event;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;

/**
 * Represents a family rebalancing its MCLoaders via it's load balancer.
 */
public class FamilyRebalanceEvent implements Event {
    protected final IFamily family;

    public FamilyRebalanceEvent(IFamily family) {
        this.family = family;
    }

    public IFamily family() {
        return this.family;
    }
}