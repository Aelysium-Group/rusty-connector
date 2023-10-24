package group.aelysium.rustyconnector.api.velocity.load_balancing;

public interface ISortable {
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
