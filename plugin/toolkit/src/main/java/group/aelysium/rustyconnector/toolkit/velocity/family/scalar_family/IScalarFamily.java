package group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.IInitialEventConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IScalarFamily<TMCLoader extends IMCLoader, TPlayer extends IPlayer> extends IFamily<TMCLoader, TPlayer>, IInitialEventConnectable<TMCLoader, TPlayer> {
    /**
     * Fetches the current server that this family's {@link ILoadBalancer} is pointing to.
     * @param player {@link TPlayer}
     * @return {@link IMCLoader}
     * @throws RuntimeException If there was an issue fetching a server.
     */
    TMCLoader fetchAny(TPlayer player) throws RuntimeException;
}
