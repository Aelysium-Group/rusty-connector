package group.aelysium.rustyconnector.toolkit.velocity.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

public interface IBaseFamily<TPlayerServer extends IPlayerServer, TResolvablePlayer extends IRustyPlayer> {
    String name();

    /**
     * Get a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     * @return A found server or `null` if there's no match.
     */
    TPlayerServer findServer(@NotNull ServerInfo serverInfo);

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    void addServer(TPlayerServer server);

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    void removeServer(TPlayerServer server);

    /**
     * Get all players in the family up to approximately `max`.
     * @param max The approximate max number of players to return.
     * @return A list of players.
     */
    List<Player> players(int max);

    List<TPlayerServer> registeredServers();

    boolean containsServer(ServerInfo serverInfo);

    /**
     * Method added for convenience.
     * Any implementation of this interface should perform some form of operation when connect is called.
     * @param player The player to ultimately connect to the family
     * @return The server that the player was connected to.
     */
    TPlayerServer connect(TResolvablePlayer player);

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    long playerCount();

    /**
     * Gets all {@link IPlayerServer PlayerServers} that are locked on this family.
     * For a list of unlocked {@link IPlayerServer PlayerServers}, use {@link IPlayerFocusedFamilyBase#loadBalancer()}.{@link ILoadBalancer#dump() dump()}.
     * @return {@link List<IPlayerServer>}
     */
    List<TPlayerServer> lockedServers();

    /**
     * Unlock a {@link IPlayerServer}, allowing players to connect to it via the load balancer.
     * If the requested server isn't registered to this family, nothing will happen.
     * @param server The {@link IPlayerServer} to unlock.
     */
    void unlockServer(TPlayerServer server);

    /**
     * Lock a {@link IPlayerServer}, preventing players from connect to it via the load balancer.
     * If the requested server isn't registered to this family, nothing will happen.
     * </p>
     * Event though a locked server can't be joined via the load balancer, you can still send players to it by using the RC send command.
     * @param server The {@link IPlayerServer} to lock.
     */
    void lockServer(TPlayerServer server);

    /**
     * Checks if the requested {@link IPlayerServer} is joinable.
     * @param server The {@link IPlayerServer} to check.
     * @return `true` if the {@link IPlayerServer} can be joined via the family's load balancer. `false` otherwise.
     */
    boolean joinable(TPlayerServer server);

    /**
     * Gets the number of {@link IPlayerServer PlayerServers} that are registered to this family.
     * This method counts both locked and unlocked servers.
     * To get the count of either locked or unlocked use: {@link IPlayerFocusedFamilyBase#lockedServers()}.{@link List#size() size()} or {@link IPlayerFocusedFamilyBase#loadBalancer()}.{@link ILoadBalancer#size() size()}
     * @return {@link Long}
     */
    long serverCount();

    /**
     * Returns this family's {@link ILoadBalancer}.
     * @return {@link ILoadBalancer}
     */
    ILoadBalancer<TPlayerServer> loadBalancer();

    /**
     * Fetches a reference to the parent of this family.
     * The parent of this family should always be either another family, or the root family.
     * If this family is the root family, this method will always return `null`.
     * @return {@link WeakReference <IBaseFamily>}
     */
    IBaseFamily<TPlayerServer, TResolvablePlayer> parent();
}
