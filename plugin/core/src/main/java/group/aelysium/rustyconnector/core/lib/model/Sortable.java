package group.aelysium.rustyconnector.core.lib.model;

public interface Sortable {
    /**
     * Get the sort index.
     * @return The index by which sorting must occur.
     */
    int sortIndex();

    /**
     * Get the weight value.
     * @return The weight of this item.
     */
    int weight();
}
