package group.aelysium.rustyconnector.toolkit.velocity.load_balancing;

import java.util.List;
import java.util.Optional;

public interface ILoadBalancer<I> {
    /**
     * Is the load balancer persistent?
     * @return `true` if the load balancer is persistent. `false` otherwise.
     */
    boolean persistent();

    /**
     * Is the load balancer weighted?
     * @return `true` if the load balancer is weighted. `false` otherwise.
     */
    boolean weighted();

    /**
     * Get the number of attempts that persistence will make.
     * @return The number of attempts.
     */
    int attempts();

    /**
     * Get the item that the iterator is currently pointing to.
     * Once this returns an item, it will automatically iterate to the next item.
     *
     * @return The item.
     */
    Optional<I> current();

    /**
     * Get the index number of the currently selected item.
     * @return The current index.
     */
    int index();

    /**
     * Iterate to the next item.
     * Some conditions might apply causing it to not truly iterate.
     */
    void iterate();

    /**
     * No matter what, iterate to the next item.
     */
    void forceIterate();

    /**
     * Sort the entire load balancer's contents.
     * Also resets the index to 0.
     */
    void completeSort();

    /**
     * Sort only one index into a new position.
     * The index chosen is this.index.
     * Also resets the index to 0.
     */
    void singleSort();

    /**
     * Add an item to the load balancer.
     */
    void add(I item);

    /**
     * Remove an item from the load balancer.
     */
    void remove(I item);

    /**
     * Return the number of servers contained in the load balancer.
     * Specifically will add up the number of locked and unlocked servers and return that number.
     * @return The number of servers.
     */
    int size();

    /**
     * Return the number of servers in the load balancer.
     * @param locked If `true`, will return the number of locked servers. If `false` will return the number of unlocked servers.
     * @return The number of servers.
     */
    int size(boolean locked);

    /**
     * Returns a list of all servers in this load balancer.
     * The returned list will contain all open and all locked servers.
     * @return {@link List<I>}
     */
    List<I> servers();

    /**
     * Return all open servers from the load balancer.
     * The returned list is separated from the list the load balancer uses. Changes to the returned list will not be reflected in the load balancer.
     * @return The servers to return.
     */
    List<I> openServers();

    /**
     * Return all locked servers from the load balancer.
     * The returned list is separated from the list the load balancer uses. Changes to the returned list will not be reflected in the load balancer.
     * @return The locked servers to return.
     */
    List<I> lockedServers();

    /**
     * Checks if the load balancer contains the specified item.
     * @return `true` if the item exists. `false` otherwise.
     */
    boolean contains(I items);

    /**
     * The load balancer as a string.
     * @return The load balancer as a string.
     */
    String toString();

    /**
     * Set the persistence of the load balancer.
     * @param persistence The persistence.
     * @param attempts The number of attempts that persistence will try to connect a player before quiting. This value doesn't matter if persistence is set to `false`
     */
    void setPersistence(boolean persistence, int attempts);

    /**
     * Set whether the load balancer is weighted.
     * @param weighted Whether the load balancer is weighted.
     */
    void setWeighted(boolean weighted);

    /**
     * Resets the index of the load balancer.
     */
    void resetIndex();

    /**
     * Locks the specific server so that the load balancer won't return it.
     * If the server is already locked, or doesn't exist in the load balancer, nothing will happen.
     * @param server The server to lock.
     */
    void lock(I server);

    /**
     * Unlocks the specific server so that the load balancer can return it.
     * If the server is already unlocked, or doesn't exist in the load balancer, nothing will happen.
     * @param server The server to unlock.
     */
    void unlock(I server);

    /**
     * Checks if the specified server will be joinable via the load balancer.
     * @param server The server to check.
     * @return `true` is the server is joinable via the load balancer. `false` if the server is locked or simply doesn't exist in the load balancer.
     */
    boolean joinable(I server);
}
