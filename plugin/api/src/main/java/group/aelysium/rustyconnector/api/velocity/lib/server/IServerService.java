package group.aelysium.rustyconnector.api.velocity.lib.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.lib.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.api.velocity.lib.serviceable.Service;

import java.lang.ref.WeakReference;
import java.util.Vector;

public interface IServerService extends Service {
    int serverTimeout();
    int serverInterval();

    /**
     * Search for a server.
     * @param serverInfo The server info to search for.
     * @return A server or `null`
     */
    IPlayerServer search(ServerInfo serverInfo);

    Vector<WeakReference<IPlayerServer>> servers();

    /**
     * Checks if a server is contained in this server service.
     * @param serverInfo The {@link ServerInfo} to search with.
     * @return {@link Boolean}
     */
    boolean contains(ServerInfo serverInfo);

    /**
     * Register a server to the proxy.
     * @param server The server to be registered.
     * @param family The family to register the server into.
     * @return A RegisteredServer node.
     */
    <S extends IPlayerServer, F extends IBaseFamily> RegisteredServer registerServer(S server, F family) throws Exception;

    /**
     * Unregister a server from the proxy.
     * @param serverInfo The server to be unregistered.
     * @param familyName The name of the family associated with the server.
     * @param removeFromFamily Should the server be removed from it's associated family?
     */
    void unregisterServer(ServerInfo serverInfo, String familyName, Boolean removeFromFamily) throws Exception;
}
