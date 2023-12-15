package group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family;

import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.family.InitiallyConnectableFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;

public interface ScalarFamily<TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>> extends Family<TMCLoader, TPlayer, TLoadBalancer>, InitiallyConnectableFamily<TMCLoader, TPlayer, TLoadBalancer> {
    /**
     * Fetches the current server that this family's {@link ILoadBalancer} is pointing to.
     * @param player {@link TPlayer}
     * @return {@link MCLoader}
     * @throws RuntimeException If there was an issue fetching a server.
     */
    TMCLoader fetchAny(TPlayer player) throws RuntimeException;
}
