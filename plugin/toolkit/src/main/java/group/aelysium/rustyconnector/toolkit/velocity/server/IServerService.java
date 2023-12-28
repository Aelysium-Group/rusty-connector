package group.aelysium.rustyconnector.toolkit.velocity.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;

import java.util.List;
import java.util.Optional;

public interface IServerService<TMCLoader extends MCLoader, TPlayer extends Player, TLoadBalancer extends ILoadBalancer<TMCLoader>, TBaseFamily extends Family<TMCLoader, TPlayer, TLoadBalancer>> extends Service {
    int serverTimeout();
    int serverInterval();

    List<TMCLoader> servers();

    /**
     * Finds an MCLoader based on server info.
     * An alternate route of getting a family, other than "tinder.services().server().find()", can be to use {@link MCLoader.Reference new MCLoader.Reference(serverInfo)}{@link MCLoader.Reference#get() .get()}.
     * @param serverInfo The info to search for.
     * @return {@link Optional <MCLoader>}
     */
    Optional<? extends MCLoader> fetch(ServerInfo serverInfo);

    /**
     * Checks if a server is contained in this server service.
     * @param serverInfo The {@link ServerInfo} to search with.
     * @return {@link Boolean}
     */
    boolean contains(ServerInfo serverInfo);
}
