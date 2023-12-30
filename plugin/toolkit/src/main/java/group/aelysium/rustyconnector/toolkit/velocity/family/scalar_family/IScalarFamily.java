package group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.InitiallyConnectableFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IScalarFamily extends IFamily, InitiallyConnectableFamily {
    /**
     * Fetches the current server that this family's {@link ILoadBalancer} is pointing to.
     * @param player {@link IPlayer}
     * @return {@link IMCLoader}
     * @throws RuntimeException If there was an issue fetching a server.
     */
    IMCLoader fetchAny(IPlayer player) throws RuntimeException;
}
