package group.aelysium.rustyconnector.toolkit.velocity.events;

import group.aelysium.rustyconnector.toolkit.core.event_factory.Event;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents a family rebalancing its MCLoaders via it's load balancer.
 */
public class FamilyRebalanceEvent implements Event {
    protected Family<MCLoader, Player, ILoadBalancer<MCLoader>> family;

    protected FamilyRebalanceEvent(Family<MCLoader, Player, ILoadBalancer<MCLoader>> family) {
        this.family = family;
    }

    public Family<MCLoader, Player, ILoadBalancer<MCLoader>> family() {
        return this.family;
    }
}