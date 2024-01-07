package group.aelysium.rustyconnector.toolkit.velocity.events.family;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

/**
 * Represents a family rebalancing its MCLoaders via it's load balancer.
 */
public class RebalanceEvent extends Cancelable {
    protected final IFamily family;

    public RebalanceEvent(IFamily family) {
        this.family = family;
    }

    public IFamily family() {
        return this.family;
    }
}