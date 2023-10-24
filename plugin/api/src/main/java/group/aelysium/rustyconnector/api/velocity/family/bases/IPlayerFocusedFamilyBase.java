package group.aelysium.rustyconnector.api.velocity.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.api.velocity.whitelist.IWhitelist;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * This class should never be used directly.
 * Player-focused families offer features such as /tpa, whitelists, load-balancing, and direct connection.
 */
public interface IPlayerFocusedFamilyBase<S extends IPlayerServer> extends IBaseFamily<S> {
    /**
     * Fetch a reference to the parent of this family.
     * @return {@link WeakReference< IBaseFamily >}
     */
    <F extends IBaseFamily<S>> WeakReference<F> parent();

    /**
     * Connect a player to this family
     * @param player The player to connect
     * @return A PlayerServer on successful connection.
     * @throws RuntimeException If the connection cannot be made.
     */
    S connect(Player player);

    /**
     * Returns this family's {@link ILoadBalancer}.
     * @return {@link ILoadBalancer}
     */
    ILoadBalancer<S> loadBalancer();
  
    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    IWhitelist whitelist();

    /**
     * Gets the number of {@link IPlayerServer PlayerServers} that are registered to this family.
     * This method counts both locked and unlocked servers.
     * To get the count of either locked or unlocked use: {@link IPlayerFocusedFamilyBase#lockedServers()}.{@link List#size() size()} or {@link IPlayerFocusedFamilyBase#loadBalancer()}.{@link ILoadBalancer#size() size()}
     * @return {@link Long}
     */
    long serverCount();

    /**
     * Lazily gets the number of players connected to this family.
     * Depending on sync values and how often players connect or disconnect from this family,
     * this value will sometimes be a few players off of the actual amount.
     * @return {@link Long}
     */
    long playerCount();

    /**
     * Gets all {@link IPlayerServer PlayerServers} that've been registered to this family.
     * This method will return all servers regardless of if they're locked or not.
     * To only get servers that are locked or unlocked, use: {@link IPlayerFocusedFamilyBase#lockedServers()} or {@link IPlayerFocusedFamilyBase#loadBalancer()}.{@link ILoadBalancer#dump() dump()}
     * @return {@link List<IPlayerServer>}
     */
    List<S> registeredServers();

    /**
     * Gets all {@link IPlayerServer PlayerServers} that are locked on this family.
     * For a list of unlocked {@link IPlayerServer PlayerServers}, use {@link IPlayerFocusedFamilyBase#loadBalancer()}.{@link ILoadBalancer#dump() dump()}.
     * @return {@link List<IPlayerServer>}
     */
    List<S> lockedServers();

    /**
     * Unlock a {@link IPlayerServer}, allowing players to connect to it via the load balancer.
     * If the requested server isn't registered to this family, nothing will happen.
     * @param server The {@link IPlayerServer} to unlock.
     */
    void unlockServer(S server);

    /**
     * Lock a {@link IPlayerServer}, preventing players from connect to it via the load balancer.
     * If the requested server isn't registered to this family, nothing will happen.
     * </p>
     * Event though a locked server can't be joined via the load balancer, you can still send players to it by using the RC send command.
     * @param server The {@link IPlayerServer} to lock.
     */
    void lockServer(S server);

    /**
     * Checks if the requested {@link IPlayerServer} is joinable.
     * @param server The {@link IPlayerServer} to check.
     * @return `true` if the {@link IPlayerServer} can be joined via the family's load balancer. `false` otherwise.
     */
    boolean joinable(S server);

    /**
     * Searches for a {@link IPlayerServer} with the requested {@link ServerInfo}.
     * This method will return a {@link IPlayerServer} regardless of if it is locked or not.
     * @param serverInfo The {@link ServerInfo} to search with.
     * @return A {@link IPlayerServer} or `null` if one couldn't be found.
     */
    S findServer(@NotNull ServerInfo serverInfo);

    /**
     * Lazily gets a list of all the players connected to this family.
     * This method uses a server chunking method for counting player counts; this means that the returned list of players might contain more players than what is set in `max`.
     * @param max The maximum number of players to fetch.
     * @return A {@link List<Player>}. The number of players in the list might be larger than `max`.
     */
    List<Player> players(int max);
}
