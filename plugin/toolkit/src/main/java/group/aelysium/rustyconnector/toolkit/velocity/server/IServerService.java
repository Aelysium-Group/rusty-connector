package group.aelysium.rustyconnector.toolkit.velocity.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;

import java.lang.ref.WeakReference;
import java.util.Vector;

public interface IServerService<TMCLoader extends IMCLoader, TPlayer extends IPlayer, TLoadBalancer extends ILoadBalancer<TMCLoader>, TBaseFamily extends IFamily<TMCLoader, TPlayer, TLoadBalancer>> extends Service {
    int serverTimeout();
    int serverInterval();

    Vector<WeakReference<TMCLoader>> servers();

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
    RegisteredServer registerServer(TMCLoader server, TBaseFamily family) throws Exception;

    /**
     * Unregister a server from the proxy.
     * @param serverInfo The server to be unregistered.
     * @param familyName The name of the family associated with the server.
     * @param removeFromFamily Should the server be removed from it's associated family?
     */
    void unregisterServer(ServerInfo serverInfo, String familyName, Boolean removeFromFamily) throws Exception;
}
