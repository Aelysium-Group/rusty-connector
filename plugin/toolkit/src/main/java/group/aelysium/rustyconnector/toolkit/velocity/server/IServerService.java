package group.aelysium.rustyconnector.toolkit.velocity.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IServerService extends Service {
    int serverTimeout();
    int serverInterval();

    List<IMCLoader> servers();

    /**
     * Finds an MCLoader based on server info.
     * An alternate route of getting a family, other than "tinder.services().server().find()", can be to use {@link IMCLoader.Reference new MCLoader.Reference(serverInfo)}{@link IMCLoader.Reference#get() .get()}.
     * @param serverInfo The info to search for.
     * @return {@link Optional<IMCLoader>}
     */
    Optional<IMCLoader> fetch(UUID serverInfo);

    /**
     * Checks if a server is contained in this server service.
     * @param serverInfo The {@link ServerInfo} to search with.
     * @return {@link Boolean}
     */
    boolean contains(UUID serverInfo);
}
