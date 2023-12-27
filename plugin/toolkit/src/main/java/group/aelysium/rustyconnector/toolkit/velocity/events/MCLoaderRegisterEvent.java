package group.aelysium.rustyconnector.toolkit.velocity.events;

import group.aelysium.rustyconnector.toolkit.core.event_factory.Event;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a family rebalancing its MCLoaders via it's load balancer.
 */

public class MCLoaderRegisterEvent implements Event {
    protected MCLoader mcLoader;

    protected MCLoaderRegisterEvent(MCLoader mcLoader) {
        this.mcLoader = mcLoader;
    }

    public MCLoader mcLoader() {
        return this.mcLoader;
    }
}