package group.aelysium.rustyconnector.core.lib.model;

import java.util.List;

public interface NodeManager<T> {
    /**
     * Finds a family which has been saved to this manager.
     * @param name The name of the family to look for
     */
    T find(String name);

    /**
     * Adds a node to the node collection.
     * @param node The node to add to this manager.
     */
    void add(T node);

    /**
     * Remove a node from the manager.
     * @param node The node to remove.
     */
    void remove(T node);

    /**
     * Dump the manager's contents.
     */
    List<T> dump();

    /**
     * Clears all nodes from the manager.
     */
    void clear();
}