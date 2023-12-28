package group.aelysium.rustyconnector.toolkit.velocity.events.mc_loader;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

/**
 * Represents an MCLoader unregistering from the Proxy.
 */
public class UnregisterEvent extends Cancelable {
    protected final Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family;
    protected final MCLoader mcLoader;

    public UnregisterEvent(Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family, MCLoader mcLoader) {
        this.family = family;
        this.mcLoader = mcLoader;
    }

    public Family<? extends MCLoader, ? extends Player, ? extends ILoadBalancer<? extends MCLoader>> family() {
        return family;
    }
    public MCLoader mcLoader() {
        return mcLoader;
    }
}