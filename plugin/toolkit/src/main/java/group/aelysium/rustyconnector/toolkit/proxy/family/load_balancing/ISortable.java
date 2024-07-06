package group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing;

public interface ISortable {
    /**
     * Get the sort index.
     * @return The index by which sorting must occur.
     */
    double sortIndex();

    /**
     * Get the weight value.
     * @return The weight of this item.
     */
    int weight();
}
