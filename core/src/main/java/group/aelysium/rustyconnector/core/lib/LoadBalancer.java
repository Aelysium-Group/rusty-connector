package group.aelysium.rustyconnector.core.lib;

import java.util.ArrayList;
import java.util.List;

public interface LoadBalancer<I> {
    int index = 0;
    int size = 0;
    List<?> items = new ArrayList<>();

    /**
     * Get the item that the iterator is currently pointing to.
     * Once this returns an item, it will automatically iterate to the next item.
     *
     * @return The item.
     */
    I getCurrent();

    /**
     * Get the index number of the currently selected item.
     * @return The current index.
     */
    int getIndex();

    /**
     * Iterate to the next item.
     */
    void iterate();

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
     * Return the number of items.
     * @return The number of items.
     */
    List<I> dump();

    /**
     * The load balancer as a string.
     * @return The load balancer as a string.
     */
    String toString();
}
