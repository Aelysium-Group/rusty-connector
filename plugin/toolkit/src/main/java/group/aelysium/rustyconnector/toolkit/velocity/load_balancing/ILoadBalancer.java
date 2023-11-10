package group.aelysium.rustyconnector.toolkit.velocity.load_balancing;

import java.util.List;

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
    I current();

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
     * Return the number of items.
     * @return The number of items.
     */
    int size();

    /**
     * Return all items from the load balancer.
     * @return The items to return.
     */
    List<I> dump();

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
}
