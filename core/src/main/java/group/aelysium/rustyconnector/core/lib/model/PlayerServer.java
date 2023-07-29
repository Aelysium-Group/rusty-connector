package group.aelysium.rustyconnector.core.lib.model;

public interface PlayerServer extends Sortable {

    /**
     * Get the number of players on this server
     * @return The player count
     */
    int playerCount();

    /**
     * Get the weight of this server
     * @return The weight of this server
     */
    int weight();

    /**
     * Get the soft player cap of this server
     * @return The soft player cap of this server
     */
    int softPlayerCap();
    /**
     * Get the hard player cap of this server
     * @return The hard player cap
     */
    int hardPlayerCap();
}
